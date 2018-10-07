package org.mechaverse.manager.service;

import static org.mechaverse.manager.service.MechaverseManagerUtil.getInactiveTasks;
import static org.mechaverse.manager.service.MechaverseManagerUtil.hasActiveTask;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.mechaverse.manager.service.model.InstanceInfo;
import org.mechaverse.manager.service.model.SimulationConfig;
import org.mechaverse.manager.service.model.SimulationConfigProperty;
import org.mechaverse.manager.service.model.SimulationInfo;
import org.mechaverse.manager.service.model.Task;
import org.mechaverse.manager.service.repository.InstanceInfoRepository;
import org.mechaverse.manager.service.repository.SimulationConfigPropertyRepository;
import org.mechaverse.manager.service.repository.SimulationConfigRepository;
import org.mechaverse.manager.service.repository.SimulationInfoRepository;
import org.mechaverse.manager.service.repository.TaskRepository;
import org.mechaverse.manager.service.storage.MechaverseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link MechaverseManagerService}.
 */
public class HibernateMechaverseManagerService implements MechaverseManagerService {

  // TODO(thorntonv): Periodically remove inactive tasks.

  private static final Predicate<SimulationInfo> ACTIVE_SIMULATION_PREDICATE =
      SimulationInfo::isActive;

  @Autowired
  private SimulationInfoRepository simulationInfoRepository;
  @Autowired
  private TaskRepository taskRepository;
  @Autowired
  private InstanceInfoRepository instanceInfoRepository;
  @Autowired
  private SimulationConfigRepository simulationConfigRepository;
  @Autowired
  private SimulationConfigPropertyRepository simulationConfigPropertyRepository;

  @Resource
  private MechaverseStorageService storageService;

  @Override
  @Transactional
  public synchronized Task getTask(String clientId) {
    Iterable<SimulationInfo> activeSimulationInfoList = getSimulationInfo().stream()
        .filter(ACTIVE_SIMULATION_PREDICATE).collect(Collectors.toList());

    // Search for an instance that has no active tasks and prefers the client.
    for (SimulationInfo simulationInfo : activeSimulationInfoList) {
      long taskMaxDurationSeconds = simulationInfo.getConfig().getTaskMaxDurationInSeconds();
      for (InstanceInfo instanceInfo : simulationInfo.getInstances()) {
        if (!hasActiveTask(instanceInfo.getExecutingTasks(), taskMaxDurationSeconds)
            && clientId.equalsIgnoreCase(instanceInfo.getPreferredClientId())) {
          return createTask(simulationInfo, instanceInfo, clientId);
        }
      }
    }

    // Search for an instance that has no active tasks.
    for (SimulationInfo simulationInfo : activeSimulationInfoList) {
      long taskMaxDurationSeconds = simulationInfo.getConfig().getTaskMaxDurationInSeconds();
      for (InstanceInfo instanceInfo : simulationInfo.getInstances()) {
        if (!hasActiveTask(instanceInfo.getExecutingTasks(), taskMaxDurationSeconds)) {
          return createTask(simulationInfo, instanceInfo, clientId);
        }
      }
    }

    // Attempt to create a new instance.
    for (SimulationInfo simulationInfo : activeSimulationInfoList) {
      int maxInstanceCount = simulationInfo.getConfig().getMaxInstanceCount();
      if (simulationInfo.getInstances().size() < maxInstanceCount) {
        InstanceInfo instanceInfo = createInstance(simulationInfo);
        return createTask(simulationInfo, instanceInfo, clientId);
      }
    }

    return null;
  }

  @Override
  @Transactional
  public void submitResult(long taskId, InputStream resultDataInput) throws Exception {
    final Task task = taskRepository.findOne(taskId);

    if (task == null) {
      return;
    }

    // TODO(thorntonv): Verify that the task belongs to the authenticated user.

    InstanceInfo instanceInfo = instanceInfoRepository.findOne(task.getInstanceId());

    if (instanceInfo == null) {
      return;
    }
    task.setCompletionTimeMillis(new Date().getTime());

    Iterables.removeIf(instanceInfo.getExecutingTasks(),
        otherTask -> task.getId().equals(otherTask.getId()));

    if (instanceInfo.getIteration() >= 0) {
      instanceInfo.setIteration(instanceInfo.getIteration() + task.getIterationCount());
    } else {
      instanceInfo.setIteration(0);
    }
    instanceInfoRepository.save(instanceInfo);
    taskRepository.delete(task);

    // Commit the result data to the storage service.
    storageService.setState(task.getSimulationId(), task.getInstanceId(),
        instanceInfo.getIteration(), resultDataInput);
  }

  @Override
  @Transactional
  public List<SimulationInfo> getSimulationInfo() {
    return Lists.newArrayList(simulationInfoRepository.findAll());
  }

  @Override
  @Transactional
  public SimulationInfo getSimulationInfo(String simulationId) {
    return simulationInfoRepository.findOne(simulationId);
  }

  @Override
  public InputStream getState(String simulationId, String instanceId, long iteration)
      throws IOException {
    return storageService.getState(simulationId, instanceId, iteration);
  }

  @Override
  @Transactional
  public SimulationInfo createSimulation(String name) {
    SimulationInfo simulationInfo = new SimulationInfo();
    simulationInfo.setSimulationId(UUID.randomUUID().toString());
    simulationInfo.setName(name);
    simulationInfo.setActive(true);
    simulationInfo.setConfig(simulationConfigRepository.save(new SimulationConfig()));
    simulationInfoRepository.save(simulationInfo);

    return simulationInfo;
  }

  @Override
  @Transactional
  public void setSimulationActive(String simulationId, boolean active) {
    SimulationInfo simulationInfo = getSimulationInfo(simulationId);
    simulationInfo.setActive(active);
    simulationInfoRepository.save(simulationInfo);
  }

  @Override
  @Transactional
  public void updateSimulationConfig(SimulationConfig updatedConfig) {
    SimulationConfig currentConfig = simulationConfigRepository.findOne(updatedConfig.getId());
    for(SimulationConfigProperty property : currentConfig.getConfigProperties()) {
      simulationConfigPropertyRepository.delete(property);
    }
    simulationConfigRepository.save(updatedConfig);
  }

  @Override
  @Transactional
  public void deleteSimulation(String simulationId) throws Exception {
    simulationInfoRepository.delete(simulationId);
    storageService.deleteSimulation(simulationId);
  }

  private InstanceInfo createInstance(SimulationInfo simulationInfo) {
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.setInstanceId(UUID.randomUUID().toString());
    // No iterations have been performed.
    instanceInfo.setIteration(-1);
    simulationInfo.getInstances().add(instanceInfo);

    simulationInfoRepository.save(simulationInfo);
    return instanceInfo;
  }

  private Task createTask(
      SimulationInfo simulationInfo, InstanceInfo instanceInfo, String clientId) {
    // Remove inactive tasks.
    for (Task task : getInactiveTasks(instanceInfo.getExecutingTasks(),
        simulationInfo.getConfig().getTaskMaxDurationInSeconds())) {
      taskRepository.delete(task);
      instanceInfo.getExecutingTasks().remove(task);
    }

    Task task = new Task();
    task.setSimulationId(simulationInfo.getSimulationId());
    task.setInstanceId(instanceInfo.getInstanceId());
    task.setIteration(instanceInfo.getIteration());
    task.setClientId(clientId);
    if (instanceInfo.getIteration() >= 0) {
      task.setIterationCount(simulationInfo.getConfig().getTaskIterationCount());
    } else {
      // No iterations have been performed, task is to create initial state.
      task.setIterationCount(0);
    }
    task.setStartTimeMillis(new Date().getTime());

    instanceInfo.getExecutingTasks().add(task);
    instanceInfo.setPreferredClientId(clientId);

    task = taskRepository.save(task);
    instanceInfoRepository.save(instanceInfo);

    return task;
  }
}
