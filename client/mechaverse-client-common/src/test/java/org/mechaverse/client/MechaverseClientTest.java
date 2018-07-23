package org.mechaverse.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.Task;
import org.mechaverse.service.storage.api.MechaverseStorageService;
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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link MechaverseClient}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
public class MechaverseClientTest {

  @Autowired private MechaverseManager mockManager;
  @Autowired private Simulation mockSimulation;
  @Autowired private MechaverseStorageService mockStorageService;

  private MechaverseClient client;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.reset(mockManager, mockSimulation, mockStorageService);

    client = new MechaverseClient(mockManager, mockStorageService, 0) {
      @Override
      protected AbstractApplicationContext getApplicationContext() {
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
    task.setIteration(300);
    task.setIterationCount(20);

    SimulationModel state = new SimulationModel();
    state.putData("test", "state".getBytes());
    SimulationModel newState = new SimulationModel();
    newState.putData("test2", "newState".getBytes());
    when(mockStorageService.getState(
        task.getSimulationId(), task.getInstanceId(), task.getIteration()))
            .thenReturn(new ByteArrayInputStream(SimulationModelUtil.serialize(state)));

    when(mockSimulation.getState()).thenReturn(newState);

    client.executeTask(task);

    ArgumentCaptor<SimulationModel> stateCaptor = ArgumentCaptor.forClass(SimulationModel.class);
    verify(mockSimulation).setState(stateCaptor.capture());
    assertArrayEquals("state".getBytes(), state.getData("test"));

    verify(mockSimulation).step(20);
    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());
    assertArrayEquals(SimulationModelUtil.serialize(newState), IOUtils.toByteArray(stateIn.getValue()));
  }

  @Test
  public void executeTask_initialState() throws Exception {
    Task task = new Task();
    task.setId(123L);
    task.setSimulationId(UUID.randomUUID().toString());
    task.setInstanceId(UUID.randomUUID().toString());
    task.setIteration(-1);
    task.setIterationCount(0);

    SimulationModel state = new SimulationModel();
    state.putData("test", "state".getBytes());
    when(mockSimulation.generateRandomState()).thenReturn(state);

    client.executeTask(task);

    verify(mockSimulation, never()).setState(any(SimulationModel.class));
    verify(mockSimulation, never()).step(anyInt());
    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());
    assertArrayEquals(SimulationModelUtil.serialize(state), IOUtils.toByteArray(stateIn.getValue()));
  }
}
