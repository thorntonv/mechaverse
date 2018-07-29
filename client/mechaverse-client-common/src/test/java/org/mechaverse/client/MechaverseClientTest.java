package org.mechaverse.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.Task;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStoreInputStream;
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

    SimulationDataStore simulationDataStore = new MemorySimulationDataStore();
    state.setIteration(300);
    simulationDataStore.put(SimulationDataStore.STATE_KEY, SimulationModelUtil.serialize(state));
    when(mockStorageService.getState(
        task.getSimulationId(), task.getInstanceId(), task.getIteration()))
        .thenReturn(SimulationDataStoreInputStream.newInputStream(simulationDataStore));

    when(mockSimulation.getStateData()).thenReturn("state2".getBytes());

    client.executeTask(task);

    verify(mockSimulation).step(20);
    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());

    simulationDataStore = new SimulationDataStoreInputStream(
        stateIn.getValue(), MemorySimulationDataStore::new).readDataStore();

    assertEquals("state2", new String(simulationDataStore.get(SimulationDataStore.STATE_KEY)));
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
    when(mockSimulation.getStateData()).thenReturn("state2".getBytes());

    client.executeTask(task);

    verify(mockSimulation, never()).step(anyInt());
    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());

    SimulationDataStore simulationDataStore = new SimulationDataStoreInputStream(
        stateIn.getValue(), MemorySimulationDataStore::new).readDataStore();

    assertEquals("state2", new String(simulationDataStore.get(SimulationDataStore.STATE_KEY)));
  }
}
