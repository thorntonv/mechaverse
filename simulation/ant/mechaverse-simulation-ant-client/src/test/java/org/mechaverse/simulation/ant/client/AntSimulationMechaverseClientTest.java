package org.mechaverse.simulation.ant.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.client.MechaverseClient;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.Task;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.ant.core.AntSimulationImpl;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.util.AntSimulationModelUtil;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStoreInputStream;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Unit test for {@link AntSimulationMechaverseClient}.
 */
public class AntSimulationMechaverseClientTest {

  @Mock
  private MechaverseManager mockManager;
  @Mock
  private MechaverseStorageService mockStorageService;

  private MechaverseClient client;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.reset(mockManager, mockStorageService);

    client = new MechaverseClient(mockManager, mockStorageService, 0);
  }

  @Test
  public void executeTask_initialState() throws Exception {
    Task task = new Task();
    task.setId(123L);
    task.setSimulationId(UUID.randomUUID().toString());
    task.setInstanceId(UUID.randomUUID().toString());
    task.setIteration(-1);
    task.setIterationCount(100);

    client.executeTask(task);

    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());

    SimulationDataStore simulationDataStore = new SimulationDataStoreInputStream(
        stateIn.getValue(), MemorySimulationDataStore::new).readDataStore();

    assertTrue(simulationDataStore.size() > 0);

    AntSimulationModel finalState = AntSimulationModelUtil
        .deserialize(simulationDataStore.get(SimulationDataStore.STATE_KEY));
    assertEquals(100, finalState.getIteration());
    assertTrue(getAntCount(finalState.getEnvironment().getEntities()) > 0);
  }

  @Test
  public void executeTask() throws Exception {
    Task task = new Task();
    task.setId(123L);
    task.setSimulationId(UUID.randomUUID().toString());
    task.setInstanceId(UUID.randomUUID().toString());
    task.setIteration(300);
    task.setIterationCount(100);

    AntSimulationImpl simulation = getApplicationContext().getBean(AntSimulationImpl.class);
    AntSimulationModel state = simulation.generateRandomState();
    SimulationDataStore simulationDataStore = new MemorySimulationDataStore();
    state.setIteration(300);
    simulationDataStore.put(SimulationDataStore.STATE_KEY, AntSimulationModelUtil.serialize(state));
    when(mockStorageService.getState(
        task.getSimulationId(), task.getInstanceId(), task.getIteration()))
        .thenReturn(SimulationDataStoreInputStream.newInputStream(simulationDataStore));

    client.executeTask(task);

    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());


    simulationDataStore = new SimulationDataStoreInputStream(
        stateIn.getValue(), MemorySimulationDataStore::new).readDataStore();

    assertTrue(simulationDataStore.size() > 0);

    AntSimulationModel finalState = AntSimulationModelUtil
        .deserialize(simulationDataStore.get(SimulationDataStore.STATE_KEY));
    assertEquals(400, finalState.getIteration());
    assertTrue(getAntCount(finalState.getEnvironment().getEntities()) > 0);
  }

  private int getAntCount(Iterable<EntityModel<EntityType>> entities) {
    int count = 0;
    for (EntityModel entity : entities) {
      if (entity instanceof Ant) {
        count++;
      }
    }
    return count;
  }

  protected AbstractApplicationContext getApplicationContext() {
    return new ClassPathXmlApplicationContext("simulation-context.xml");
  }
}
