package org.mechaverse.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.manager.api.MechaverseManagerApi;
import org.mechaverse.manager.api.model.SimulationConfig;
import org.mechaverse.manager.api.model.SimulationInfo;
import org.mechaverse.manager.api.model.Task;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.SimulationModelUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for {@link MechaverseClient}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
public class MechaverseClientTest {

  @Autowired
  private MechaverseManagerApi mockManager;
  @Autowired
  private Simulation mockSimulation;

  private MechaverseClient client;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.reset(mockManager, mockSimulation);

    client = new MechaverseClient("test-client", mockManager, 0) {
      @Override
      protected AbstractApplicationContext getApplicationContext(String simulationType) {
        return null;
      }

      @Override
      protected Simulation createSimulation(ApplicationContext ctx) {
        return mockSimulation;
      }
    };
  }

  @Test
  public void executeTask() throws Exception {
    Task task = new Task();
    task.setId(123L);
    task.setSimulationId(UUID.randomUUID().toString());
    task.setInstanceId(UUID.randomUUID().toString());
    task.setIteration(300L);
    task.setIterationCount(20);

    SimulationModel state = new SimulationModel();

    state.setIteration(300);
    SimulationInfo simulationInfo = new SimulationInfo();
    simulationInfo.setConfig(new SimulationConfig());
    simulationInfo.getConfig().setSimulationType("ant");
    when(mockManager.getSimulationInfo(task.getSimulationId())).thenReturn(simulationInfo);
    when(mockManager.getState(
        task.getSimulationId(), task.getInstanceId(), task.getIteration()))
        .thenReturn(SimulationModelUtil.serialize(state));

    when(mockSimulation.getStateData()).thenReturn("state2".getBytes());

    client.executeTask(task);

    verify(mockSimulation).step(20);
    ArgumentCaptor<byte[]> stateIn = ArgumentCaptor.forClass(byte[].class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());

    assertEquals("state2", new String(stateIn.getValue()));
  }

  @Test
  public void executeTask_initialState() throws Exception {
    Task task = new Task();
    task.setId(123L);
    task.setSimulationId(UUID.randomUUID().toString());
    task.setInstanceId(UUID.randomUUID().toString());
    task.setIteration(-1L);
    task.setIterationCount(0);

    SimulationModel state = new SimulationModel();
    state.putData("test", "state".getBytes());
    when(mockSimulation.generateRandomState()).thenReturn(state);
    SimulationInfo simulationInfo = new SimulationInfo();
    simulationInfo.setConfig(new SimulationConfig());
    simulationInfo.getConfig().setSimulationType("ant");
    when(mockManager.getSimulationInfo(task.getSimulationId())).thenReturn(simulationInfo);
    when(mockSimulation.getStateData()).thenReturn("state2".getBytes());

    client.executeTask(task);

    verify(mockSimulation, never()).step(anyInt());
    ArgumentCaptor<byte[]> stateIn = ArgumentCaptor.forClass(byte[].class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());

    assertEquals("state2", new String(stateIn.getValue()));
  }
}
