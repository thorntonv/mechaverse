package org.mechaverse.manager.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * Unit test for {@link HibernateMechaverseManagerService}.
 */

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration("/manager-test-context.xml")
@EntityScan(basePackageClasses = {SimulationInfo.class})
@EnableJpaRepositories(basePackageClasses = {SimulationInfoRepository.class})
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class HibernateMechaverseManagerServiceTest {

  private static final String TEST_CLIENT_ID = "test-client";

  @Autowired
  private MechaverseManagerService service;
  @Autowired
  private MechaverseStorageService mockStorageService;
  @Autowired
  private SimulationInfoRepository simulationInfoRepository;
  @Autowired
  private SimulationConfigRepository simulationConfigRepository;
  @Autowired
  private SimulationConfigPropertyRepository simulationConfigPropertyRepository;
  @Autowired
  private InstanceInfoRepository instanceInfoRepository;
  @Autowired
  private TaskRepository taskRepository;

  @Test
  public void hasServiceImpl() {
    assertNotNull(service);
  }

  @Test
  public void createSimulation() {
    SimulationInfo expectedSimulationInfo = service.createSimulation("test");
    List<SimulationInfo> simulationInfoList = service.getSimulationInfo();
    assertEquals(1, simulationInfoList.size());
    SimulationInfo simulationInfo = simulationInfoList.get(0);
    assertEquals(expectedSimulationInfo.getSimulationId(), simulationInfo.getSimulationId());
    assertNotNull(simulationInfo.getConfig());
    assertEquals(0, simulationInfo.getInstances().size());
  }

  @Test
  public void getSimulationById() {
    SimulationInfo simulationInfo = service.createSimulation("test");

    SimulationInfo retrievedSimulationInfo =
        service.getSimulationInfo(simulationInfo.getSimulationId());
    assertEquals(simulationInfo.getSimulationId(), retrievedSimulationInfo.getSimulationId());
  }

  @Test
  public void updateSimulationConfig() {
    SimulationInfo simulationInfo = service.createSimulation("test");

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
  public void deleteSimulationConfigProperty() {
    SimulationInfo simulationInfo = service.createSimulation("test");

    SimulationConfig config = new SimulationConfig();
    config.setId(simulationInfo.getConfig().getId());
    config.getConfigProperties().add(createProperty("test1", "first".getBytes()));
    config.getConfigProperties().add(createProperty("test2", "second".getBytes()));
    service.updateSimulationConfig(config);

    config = new SimulationConfig();
    config.setId(simulationInfo.getConfig().getId());
    config.getConfigProperties().add(createProperty("test3", "third".getBytes()));
    service.updateSimulationConfig(config);

    assertEquals(1, Iterables.size(simulationConfigPropertyRepository.findAll()));
  }

  @Test
  public void getTask_noSimulations() {
    assertNull(service.getTask(TEST_CLIENT_ID));
  }

  @Test
  public void getTask() {
    SimulationInfo simulationInfo = service.createSimulation("test");
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    service.updateSimulationConfig(simulationInfo.getConfig());

    Task task = service.getTask(TEST_CLIENT_ID);
    assertNotNull(task);
    assertNotNull(task.getId());
    assertEquals("test-client", task.getClientId());
    assertEquals(-1, task.getIteration());
    assertEquals(0, task.getIterationCount());
    assertEquals(simulationInfo.getSimulationId(), task.getSimulationId());

    simulationInfo = service.getSimulationInfo(simulationInfo.getSimulationId());
    assertEquals(1, simulationInfo.getInstances().size());

    InstanceInfo instanceInfo = Iterables.getOnlyElement(simulationInfo.getInstances());
    assertEquals(instanceInfo.getInstanceId(), task.getInstanceId());
    assertEquals(task.getClientId(), instanceInfo.getPreferredClientId());

    assertNotEquals(simulationInfo.getSimulationId(), instanceInfo.getInstanceId());

    assertEquals(1, instanceInfo.getExecutingTasks().size());
    assertEquals(task.getId(), Iterables.getOnlyElement(instanceInfo.getExecutingTasks()).getId());
  }

  @Test
  public void submitResult() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation("test");
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    service.updateSimulationConfig(simulationInfo.getConfig());

    byte[] resultData = "simulation state".getBytes();
    Task task = service.getTask(TEST_CLIENT_ID);
    ByteArrayInputStream resultDataInput = new ByteArrayInputStream(resultData);
    service.submitResult(task.getId(), resultDataInput);

    simulationInfo = service.getSimulationInfo(simulationInfo.getSimulationId());
    InstanceInfo instanceInfo = Iterables.getOnlyElement(simulationInfo.getInstances());
    assertEquals(0, instanceInfo.getIteration());
    assertEquals(0, instanceInfo.getExecutingTasks().size());

    verifyNoObjects(taskRepository);

    // Verify that the state was sent to the storage service.

    ArgumentCaptor<InputStream> stateInputCaptor = ArgumentCaptor.forClass(InputStream.class);
    verify(mockStorageService).setState(
        eq(task.getSimulationId()), eq(task.getInstanceId()), eq(instanceInfo.getIteration()),
        stateInputCaptor.capture());

    InputStream stateInput = stateInputCaptor.getValue();
    assertArrayEquals(resultData, ByteStreams.toByteArray(stateInput));
  }

  @Test
  public void getTask_multipleInstances() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation("test");
    simulationInfo.getConfig().setMaxInstanceCount(10);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    simulationInfo.getConfig().setTaskMaxDurationInSeconds(300);
    service.updateSimulationConfig(simulationInfo.getConfig());

    byte[] state = "simulation state".getBytes();
    Task task1 = service.getTask(TEST_CLIENT_ID);
    Task task2 = service.getTask(TEST_CLIENT_ID);
    Task task3 = service.getTask(TEST_CLIENT_ID);

    service.submitResult(task1.getId(), new ByteArrayInputStream(state));
    service.submitResult(task2.getId(), new ByteArrayInputStream(state));
    service.submitResult(task3.getId(), new ByteArrayInputStream(state));

    List<SimulationInfo> simulationInfoList = service.getSimulationInfo();
    assertEquals(1, simulationInfoList.size());
    assertEquals(3, simulationInfoList.get(0).getInstances().size());
  }

  @Test
  public void getTask_afterSubmit() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation("test");
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    simulationInfo.getConfig().setTaskMaxDurationInSeconds(300);
    service.updateSimulationConfig(simulationInfo.getConfig());

    byte[] state = "simulation state".getBytes();
    Task task = service.getTask(TEST_CLIENT_ID);
    service.submitResult(task.getId(), new ByteArrayInputStream(state));

    task = service.getTask(TEST_CLIENT_ID);
    assertEquals(0, task.getIteration());
    service.submitResult(task.getId(), new ByteArrayInputStream(state));

    task = service.getTask(TEST_CLIENT_ID);
    assertEquals(60 * 300, task.getIteration());
  }

  @Test
  public void getTask_inactiveTask() {
    SimulationInfo simulationInfo = service.createSimulation("test");
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    simulationInfo.getConfig().setTaskMaxDurationInSeconds(300);
    service.updateSimulationConfig(simulationInfo.getConfig());

    Task task = service.getTask(TEST_CLIENT_ID);
    task.setStartTimeMillis(task.getStartTimeMillis() - 600 * 1000);
    taskRepository.save(task);

    simulationInfo = service.getSimulationInfo(simulationInfo.getSimulationId());
    InstanceInfo instanceInfo = Iterables.getOnlyElement(simulationInfo.getInstances());
    instanceInfo.setPreferredClientId("other-test-client");
    instanceInfoRepository.save(instanceInfo);

    task = service.getTask(TEST_CLIENT_ID);
    assertEquals(-1, task.getIteration());
  }

  @Test
  public void getTask_simulationMaxTasks() {
    SimulationInfo simulationInfo = service.createSimulation("test");
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    simulationInfo.getConfig().setTaskMaxDurationInSeconds(300);
    service.updateSimulationConfig(simulationInfo.getConfig());

    Task task = service.getTask(TEST_CLIENT_ID);
    assertNotNull(task);
    task = service.getTask(TEST_CLIENT_ID);
    assertNull(task);
  }

  @Test
  public void getTask_inactiveSimulation() {
    SimulationInfo simulationInfo = service.createSimulation("test");
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    simulationInfo.getConfig().setTaskMaxDurationInSeconds(300);
    service.updateSimulationConfig(simulationInfo.getConfig());
    service.setSimulationActive(simulationInfo.getSimulationId(), false);

    Task task = service.getTask(TEST_CLIENT_ID);
    assertNull(task);
  }

  @Test
  public void deleteSimulation() throws Exception {
    SimulationInfo simulationInfo = service.createSimulation("test");
    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(60 * 300);
    simulationInfo.getConfig().setTaskMaxDurationInSeconds(300);
    simulationInfo.getConfig().getConfigProperties()
        .add(createProperty("test1", "This is a test".getBytes()));
    service.updateSimulationConfig(simulationInfo.getConfig());

    Task task = service.getTask(TEST_CLIENT_ID);
    assertNotNull(task);
    assertEquals(1, service.getSimulationInfo().size());

    service.deleteSimulation(simulationInfo.getSimulationId());

    assertEquals(0, service.getSimulationInfo().size());

    verify(mockStorageService).deleteSimulation(simulationInfo.getSimulationId());

    verifyNoObjects(simulationInfoRepository);
    verifyNoObjects(instanceInfoRepository);
    verifyNoObjects(taskRepository);
    verifyNoObjects(simulationConfigRepository);
    verifyNoObjects(simulationConfigPropertyRepository);
  }

  @Test
  public void setActive() {
    SimulationInfo simulationInfo = service.createSimulation("test");
    assertTrue(simulationInfo.isActive());
    service.setSimulationActive(simulationInfo.getSimulationId(), false);
    assertFalse(service.getSimulationInfo(simulationInfo.getSimulationId()).isActive());
    service.setSimulationActive(simulationInfo.getSimulationId(), true);
    assertTrue(service.getSimulationInfo(simulationInfo.getSimulationId()).isActive());
  }

  private SimulationConfigProperty createProperty(String name, byte[] value) {
    SimulationConfigProperty property = new SimulationConfigProperty();
    property.setName(name);
    property.setValue(value);
    return property;
  }

  @SuppressWarnings("unchecked")
  private <T, ID extends Serializable> void verifyNoObjects(CrudRepository<T, ID> repository) {
    assertEquals(0, Iterables.size(repository.findAll()));
  }
}
