package org.mechaverse.service.manager;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.Criteria;
import org.hibernate.Query;
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

  @Autowired private SessionFactory sessionFactory;
  @Resource private MechaverseStorageService storageService;

  @Override
  public Task getTask() throws Exception {
    // TODO(thorntonv): Get client id.
    String clientId = "test-client";

    List<SimulationInfo> simulationInfoList = getSimulationInfo();

    // Search for an instance that has no active tasks and prefers the client.
    for (SimulationInfo simulationInfo : simulationInfoList) {
      long taskMaxDurationSeconds = simulationInfo.getConfig().getTaskMaxDurationInSeconds();
      for (InstanceInfo instanceInfo : simulationInfo.getInstances()) {
        if (!hasActiveTask(instanceInfo, taskMaxDurationSeconds)
            && clientId.equalsIgnoreCase(instanceInfo.getPreferredClientId())) {
          return createTask(simulationInfo, instanceInfo, clientId);
        }
      }
    }

    // Search for an instance that has no active tasks.
    for (SimulationInfo simulationInfo : simulationInfoList) {
      long taskMaxDurationSeconds = simulationInfo.getConfig().getTaskMaxDurationInSeconds();
      for (InstanceInfo instanceInfo : simulationInfo.getInstances()) {
        if (!hasActiveTask(instanceInfo, taskMaxDurationSeconds)) {
          return createTask(simulationInfo, instanceInfo, clientId);
        }
      }
    }

    // Attempt to create a new instance.
    for (SimulationInfo simulationInfo : simulationInfoList) {
      int maxInstanceCount = simulationInfo.getConfig().getMaxInstanceCount();
      if (simulationInfo.getInstances().size() < maxInstanceCount) {
        InstanceInfo instanceInfo = createInstance(simulationInfo);
        return createTask(simulationInfo, instanceInfo, clientId);
      }
    }

    return null;
  }

  @Override
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

    instanceInfo.setIteration(instanceInfo.getIteration() + task.getIterationCount());
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

    Criteria criteria = session.createCriteria(SimulationInfo.class);

    @SuppressWarnings("unchecked")
    List<SimulationInfo> simulationInfoList = criteria.list();

    return simulationInfoList;
  }

  @Override
  public SimulationInfo getSimulationInfo(String simulationId) {
    Session session = sessionFactory.getCurrentSession();

    Criteria criteria = session.createCriteria(SimulationInfo.class);
    criteria.add(Expression.eq("simulationId", simulationId));
    SimulationInfo simulationInfo = (SimulationInfo) criteria.uniqueResult();

    return simulationInfo;
  }

  @Override
  @Transactional
  public SimulationInfo createSimulation() {
    Session session = sessionFactory.getCurrentSession();

    SimulationInfo simulationInfo = new SimulationInfo();
    simulationInfo.setSimulationId(UUID.randomUUID().toString());
    simulationInfo.setConfig(new SimulationConfig());
    session.save(simulationInfo);

    return simulationInfo;
  }

  @Override
  @Transactional
  public void updateSimulationConfig(SimulationConfig updatedConfig) throws Exception {
    Session session = sessionFactory.getCurrentSession();
    SimulationConfig currentConfig =
        (SimulationConfig) session.get(SimulationConfig.class, updatedConfig.getId());
    // TODO(thorntonv): Determine why orphan removal isn't working.
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

    Query q = session.createQuery("delete from SimulationInfo where simulationId = :id");
    q.setString("id", simulationId);
    q.executeUpdate();
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
    instanceInfo.setIteration(0);
    simulationInfo.getInstances().add(instanceInfo);

    instanceInfo.setInstanceId((String) sessionFactory.getCurrentSession().save(simulationInfo));
    return instanceInfo;
  }

  private Task createTask(
      SimulationInfo simulationInfo, InstanceInfo instanceInfo, String clientId) {
    Task task = new Task();
    task.setSimulationId(simulationInfo.getSimulationId());
    task.setInstanceId(instanceInfo.getInstanceId());
    task.setIteration(instanceInfo.getIteration());
    task.setClientId(clientId);
    task.setIterationCount(simulationInfo.getConfig().getTaskIterationCount());
    task.setStartTime(new Timestamp(new Date().getTime()));

    instanceInfo.getExecutingTasks().add(task);
    instanceInfo.setPreferredClientId(clientId);

    sessionFactory.getCurrentSession().save(instanceInfo);

    return task;
  }
}