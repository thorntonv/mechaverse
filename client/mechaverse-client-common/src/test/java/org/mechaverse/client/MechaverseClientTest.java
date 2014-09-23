package org.mechaverse.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.mechaverse.simulation.api.Simulation;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
      protected Simulation createSimulation() {
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

    SimulationDataStore state = new SimulationDataStore();
    state.put("test", "state".getBytes());
    SimulationDataStore newState = new SimulationDataStore();
    newState.put("test2", "newState".getBytes());
    when(mockStorageService.getState(
        task.getSimulationId(), task.getInstanceId(), task.getIteration()))
            .thenReturn(new ByteArrayInputStream(state.serialize()));

    when(mockSimulation.getState()).thenReturn(newState);

    client.executeTask(task);

    ArgumentCaptor<SimulationDataStore> stateCaptor = ArgumentCaptor.forClass(SimulationDataStore.class);
    verify(mockSimulation).setState(stateCaptor.capture());
    assertEquals(state.keySet(), stateCaptor.getValue().keySet());
    assertArrayEquals("state".getBytes(), state.get("test"));

    verify(mockSimulation).step(20);
    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());
    assertArrayEquals(newState.serialize(), IOUtils.toByteArray(stateIn.getValue()));
  }

  @Test
  public void executeTask_initialState() throws Exception {
    Task task = new Task();
    task.setId(123L);
    task.setSimulationId(UUID.randomUUID().toString());
    task.setInstanceId(UUID.randomUUID().toString());
    task.setIteration(-1);
    task.setIterationCount(0);

    SimulationDataStore state = new SimulationDataStore();
    state.put("test", "state".getBytes());
    when(mockSimulation.generateRandomState()).thenReturn(state);

    client.executeTask(task);

    verify(mockSimulation, never()).setState(any(SimulationDataStore.class));
    verify(mockSimulation, never()).step(anyInt());
    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());
    assertArrayEquals(state.serialize(), IOUtils.toByteArray(stateIn.getValue()));
  }
}
