package org.mechaverse.service.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.InstanceInfo;
import org.mechaverse.service.manager.api.model.SimulationConfig;
import org.mechaverse.service.manager.api.model.SimulationConfigProperty;
import org.mechaverse.service.manager.api.model.SimulationInfo;
import org.mechaverse.service.manager.api.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

/**
 * Unit test for {@link MechaverseManagerImpl}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class MechaverseManagerImplTest {

  @Autowired private SessionFactory sessionFactory;
  @Autowired private MechaverseManager service;

  @Test
  public void hasSessionFactory() {
    assertNotNull(sessionFactory);
  }

  @Test
  public void hasServiceImpl() {
    assertNotNull(service);
  }

  @Test
  public void createSimulation() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation();
    List<SimulationInfo> simulationInfoList = service.getSimulationInfo();
    assertEquals(1, simulationInfoList.size());
    assertEquals(simulationInfo.getSimulationId(), simulationInfoList.get(0).getSimulationId());
    assertNotNull(simulationInfo.getConfig());
    assertEquals(0, simulationInfo.getInstances().size());
  }

  @Test
  public void getSimulationById() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation();

    SimulationInfo retrievedSimulationInfo =
        service.getSimulationInfo(simulationInfo.getSimulationId());
    assertEquals(simulationInfo.getSimulationId(), retrievedSimulationInfo.getSimulationId());
  }

  @Test
  public void updateSimulationConfig() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation();
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().evict(simulationInfo.getConfig());
    sessionFactory.getCurrentSession().evict(simulationInfo);

    SimulationConfig config = new SimulationConfig();
    config.setId(simulationInfo.getConfig().getId());
    config.setMinInstanceCount(0);
    config.setMaxInstanceCount(100);
    config.setTaskIterationCount(15 * 60 * 300);
    config.setTaskMaxDurationInSeconds(4 * 60 * 60);
    SimulationConfigProperty testProperty1 = new SimulationConfigProperty();
    testProperty1.setName("test1");
    testProperty1.setValue("This is a test".getBytes());
    config.getConfigProperties().add(new SimulationConfigProperty());
    service.updateSimulationConfig(config);

    simulationInfo = service.getSimulationInfo(simulationInfo.getSimulationId());
    assertEquals(0, simulationInfo.getConfig().getMinInstanceCount());
    assertEquals(100, simulationInfo.getConfig().getMaxInstanceCount());
    assertEquals(15 * 60 * 300, simulationInfo.getConfig().getTaskIterationCount());
    assertEquals(4 * 60 * 60, simulationInfo.getConfig().getTaskMaxDurationInSeconds());
    assertEquals(1, simulationInfo.getConfig().getConfigProperties().size());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void deleteSimulationConfigProperty() throws Exception {
    sessionFactory.getCurrentSession().setFlushMode(FlushMode.ALWAYS);

    SimulationInfo simulationInfo = service.createSimulation();
    sessionFactory.getCurrentSession().flush();

    SimulationConfig config = new SimulationConfig();
    config.setId(simulationInfo.getConfig().getId());
    config.getConfigProperties().add(createProperty("test1", "first".getBytes()));
    config.getConfigProperties().add(createProperty("test2", "second".getBytes()));
    service.updateSimulationConfig(config);
    sessionFactory.getCurrentSession().flush();

    Session session = sessionFactory.getCurrentSession();

    config = new SimulationConfig();
    config.setId(simulationInfo.getConfig().getId());
    config.getConfigProperties().add(createProperty("test3", "third".getBytes()));
    service.updateSimulationConfig(config);
    sessionFactory.getCurrentSession().flush();

    Criteria criteria = session.createCriteria(SimulationConfigProperty.class);
    List<SimulationConfigProperty> properties = criteria.list();
    assertEquals(1, properties.size());
  }

  @Test
  public void getTask_noSimulations() throws Exception {
    assertEquals(null, service.getTask());
  }

  @Test
  public void getTask() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation();
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60*300);
    service.updateSimulationConfig(simulationInfo.getConfig());

    Task task = service.getTask();
    assertNotNull(task);
    assertNotNull(task.getId());
    assertEquals("test-client", task.getClientId());
    assertEquals(0, task.getIteration());
    assertEquals(60*300, task.getIterationCount());
    assertEquals(simulationInfo.getSimulationId(), task.getSimulationId());

    simulationInfo = service.getSimulationInfo(simulationInfo.getSimulationId());
    assertEquals(1, simulationInfo.getInstances().size());

    InstanceInfo instanceInfo = Iterables.getOnlyElement(simulationInfo.getInstances());
    assertEquals(instanceInfo.getInstanceId(), task.getInstanceId());
    assertEquals(task.getClientId(), instanceInfo.getPreferredClientId());

    assertEquals(1, instanceInfo.getExecutingTasks().size());
    assertEquals(task.getId(), Iterables.getOnlyElement(instanceInfo.getExecutingTasks()).getId());
  }

  @Test
  public void submitResult() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation();
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    service.updateSimulationConfig(simulationInfo.getConfig());

    Task task = service.getTask();
    ByteArrayInputStream resultDataInput = new ByteArrayInputStream("simulation state".getBytes());
    service.submitResult(task.getId(), resultDataInput);

    simulationInfo = service.getSimulationInfo(simulationInfo.getSimulationId());
    InstanceInfo instanceInfo = Iterables.getOnlyElement(simulationInfo.getInstances());
    assertEquals(60 * 300, instanceInfo.getIteration());
    assertEquals(0, instanceInfo.getExecutingTasks().size());

    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Task.class);
    @SuppressWarnings("unchecked")
    List<Task> taskList = criteria.list();

    assertEquals(0, taskList.size());
  }

  @Test
  public void getTask_afterSubmit() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation();
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    simulationInfo.getConfig().setTaskMaxDurationInSeconds(300);
    service.updateSimulationConfig(simulationInfo.getConfig());

    Task task = service.getTask();
    ByteArrayInputStream resultDataInput = new ByteArrayInputStream("simulation state".getBytes());
    service.submitResult(task.getId(), resultDataInput);

    task = service.getTask();
    assertEquals(60 * 300, task.getIteration());
  }

  @Test
  public void getTask_inactiveTask() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation();
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    simulationInfo.getConfig().setTaskMaxDurationInSeconds(300);
    service.updateSimulationConfig(simulationInfo.getConfig());

    Task task = service.getTask();
    task.setStartTime(new Timestamp(task.getStartTime().getTime() - 600 * 1000));
    sessionFactory.getCurrentSession().save(task);
    InstanceInfo instanceInfo = Iterables.getOnlyElement(simulationInfo.getInstances());
    instanceInfo.setPreferredClientId("other-test-client");
    sessionFactory.getCurrentSession().save(instanceInfo);

    task = service.getTask();
    assertEquals(0, task.getIteration());
  }

  @Test
  public void getTask_simulationMaxTasks() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation();
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    simulationInfo.getConfig().setTaskMaxDurationInSeconds(300);
    service.updateSimulationConfig(simulationInfo.getConfig());

    Task task = service.getTask();
    assertNotNull(task);
    task = service.getTask();
    assertNull(task);
  }

  @Test
  public void deleteSimulation() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation();
    assertEquals(1, service.getSimulationInfo().size());
    service.deleteSimulation(simulationInfo.getSimulationId());
    assertEquals(0, service.getSimulationInfo().size());
  }

  private SimulationConfigProperty createProperty(String name, byte[] value) {
    SimulationConfigProperty property = new SimulationConfigProperty();
    property.setName(name);
    property.setValue(value);
    return property;
  }
}
