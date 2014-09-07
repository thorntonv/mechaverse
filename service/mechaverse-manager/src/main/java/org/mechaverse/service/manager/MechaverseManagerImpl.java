package org.mechaverse.service.manager;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.InstanceInfo;
import org.mechaverse.service.manager.api.model.SimulationConfig;
import org.mechaverse.service.manager.api.model.SimulationConfigProperty;
import org.mechaverse.service.manager.api.model.SimulationInfo;
import org.mechaverse.service.manager.api.model.Task;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Implementation of {@link MechaverseManager}.
 */
public class MechaverseManagerImpl implements MechaverseManager {

  // TODO(thorntonv): Periodically remove inactive tasks.

  private static final Predicate<SimulationInfo> ACTIVE_SIMULATION_PREDICATE =
      new Predicate<SimulationInfo>() {
        @Override
        public boolean apply(SimulationInfo simulationInfo) {
          return simulationInfo.isActive();
        }
      };

  @Autowired private SessionFactory sessionFactory;
  @Resource private MechaverseStorageService storageService;

  @Override
  @Transactional
  public Task getTask() throws Exception {
    // TODO(thorntonv): Get client id.
    String clientId = "test-client";

    Iterable<SimulationInfo> activeSimulationInfoList = Iterables.filter(
      getSimulationInfo(), ACTIVE_SIMULATION_PREDICATE);

    // Search for an instance that has no active tasks and prefers the client.
    for (SimulationInfo simulationInfo : activeSimulationInfoList) {
      long taskMaxDurationSeconds = simulationInfo.getConfig().getTaskMaxDurationInSeconds();
      for (InstanceInfo instanceInfo : simulationInfo.getInstances()) {
        if (!hasActiveTask(instanceInfo, taskMaxDurationSeconds)
            && clientId.equalsIgnoreCase(instanceInfo.getPreferredClientId())) {
          return createTask(simulationInfo, instanceInfo, clientId);
        }
      }
    }

    // Search for an instance that has no active tasks.
    for (SimulationInfo simulationInfo : activeSimulationInfoList) {
      long taskMaxDurationSeconds = simulationInfo.getConfig().getTaskMaxDurationInSeconds();
      for (InstanceInfo instanceInfo : simulationInfo.getInstances()) {
        if (!hasActiveTask(instanceInfo, taskMaxDurationSeconds)) {
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
    Session session = sessionFactory.getCurrentSession();
    final Task task = (Task) session.get(Task.class, taskId);

    if (task == null) {
      return;
    }

    // TODO(thorntonv): Verify that the task belongs to the authenticated user.

    InstanceInfo instanceInfo =
        (InstanceInfo) session.get(InstanceInfo.class, task.getInstanceId());

    if (instanceInfo == null) {
      return;
    }
    task.setCompletionTime(new Timestamp(new Date().getTime()));

    Iterables.removeIf(instanceInfo.getExecutingTasks(), new Predicate<Task>() {
      @Override
      public boolean apply(Task otherTask) {
        return task.getId().equals(otherTask.getId());
      }
    });

    if (instanceInfo.getIteration() >= 0) {
      instanceInfo.setIteration(instanceInfo.getIteration() + task.getIterationCount());
    } else {
      instanceInfo.setIteration(0);
    }
    session.save(instanceInfo);
    session.delete(task);

    // Commit the result data to the storage service.
    storageService.setState(task.getSimulationId(), task.getInstanceId(),
        instanceInfo.getIteration(), resultDataInput);
  }

  @Override
  @Transactional
  public List<SimulationInfo> getSimulationInfo() {
    Session session = sessionFactory.getCurrentSession();

    Criteria criteria = session.createCriteria(SimulationInfo.class)
        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    @SuppressWarnings("unchecked")
    List<SimulationInfo> simulationInfoList = criteria.list();

    return simulationInfoList;
  }

  @Override
  @Transactional
  public SimulationInfo getSimulationInfo(String simulationId) {
    Session session = sessionFactory.getCurrentSession();

    Criteria criteria = session.createCriteria(SimulationInfo.class);
    criteria.add(Expression.eq("simulationId", simulationId));
    SimulationInfo simulationInfo = (SimulationInfo) criteria.uniqueResult();

    return simulationInfo;
  }

  @Override
  @Transactional
  public SimulationInfo createSimulation(String name) {
    Session session = sessionFactory.getCurrentSession();

    SimulationInfo simulationInfo = new SimulationInfo();
    simulationInfo.setSimulationId(UUID.randomUUID().toString());
    simulationInfo.setName(name);
    simulationInfo.setActive(true);
    simulationInfo.setConfig(new SimulationConfig());
    session.save(simulationInfo);

    return simulationInfo;
  }

  @Override
  @Transactional
  public void setSimulationActive(String simulationId, boolean active) {
    SimulationInfo simulationInfo = getSimulationInfo(simulationId);
    simulationInfo.setActive(active);
    sessionFactory.getCurrentSession().save(simulationInfo);
  }

  @Override
  @Transactional
  public void updateSimulationConfig(SimulationConfig updatedConfig) throws Exception {
    Session session = sessionFactory.getCurrentSession();
    SimulationConfig currentConfig =
        (SimulationConfig) session.get(SimulationConfig.class, updatedConfig.getId());
    // TODO(thorntonv): Determine why orphan removal isn't working.
    if(currentConfig == null) {
      throw new IllegalStateException("Configuration with id " + updatedConfig.getId()
          + " does not exist.");
    }
    for(SimulationConfigProperty property : currentConfig.getConfigProperties()) {
      session.delete(property);
    }
    currentConfig.getConfigProperties().clear();
    session.merge(updatedConfig);
  }

  @Override
  @Transactional
  public void deleteSimulation(String simulationId) throws Exception {
    Session session = sessionFactory.getCurrentSession();

    SimulationInfo simulationInfo = getSimulationInfo(simulationId);
    session.delete(simulationInfo);

    storageService.deleteSimulation(simulationId);
  }

  private boolean hasActiveTask(InstanceInfo instanceInfo, long taskMaxDurationSeconds) {
    for (Task task : instanceInfo.getExecutingTasks()) {
      if (isActive(task, taskMaxDurationSeconds)) {
        return true;
      }
    }
    return false;
  }

  private boolean isActive(Task task, long taskMaxDurationSeconds) {
    long taskMaxDurationMS = taskMaxDurationSeconds * 1000;
    long now = new Date().getTime();
    return (now - task.getStartTime().getTime() < taskMaxDurationMS);
  }

  private InstanceInfo createInstance(SimulationInfo simulationInfo) {
    InstanceInfo instanceInfo = new InstanceInfo();
    instanceInfo.setInstanceId(UUID.randomUUID().toString());
    // No iterations have been performed.
    instanceInfo.setIteration(-1);
    simulationInfo.getInstances().add(instanceInfo);

    sessionFactory.getCurrentSession().save(simulationInfo);
    return instanceInfo;
  }

  private Task createTask(
      SimulationInfo simulationInfo, InstanceInfo instanceInfo, String clientId) {
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
    task.setStartTime(new Timestamp(new Date().getTime()));

    instanceInfo.getExecutingTasks().add(task);
    instanceInfo.setPreferredClientId(clientId);

    Long taskId = (Long) sessionFactory.getCurrentSession().save(task);
    task.setId(taskId);
    sessionFactory.getCurrentSession().save(instanceInfo);

    return task;
  }
}
