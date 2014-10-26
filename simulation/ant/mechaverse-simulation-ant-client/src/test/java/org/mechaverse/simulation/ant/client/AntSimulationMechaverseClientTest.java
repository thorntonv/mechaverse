package org.mechaverse.simulation.ant.client;

import static org.junit.Assert.assertTrue;
import static org.mechaverse.simulation.common.datastore.SimulationDataStoreOutputStream.toByteArray;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.client.MechaverseClient;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.Task;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.core.AntSimulationImpl;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for {@link AntSimulationMechaverseClient}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
public class AntSimulationMechaverseClientTest {

  @Autowired private MechaverseManager mockManager;
  @Autowired private MechaverseStorageService mockStorageService;

  private MechaverseClient client;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.reset(mockManager, mockStorageService);

    client = new MechaverseClient(mockManager, mockStorageService, 0);
  }

  @Test
  public void executeTask() throws Exception {
    Task task = new Task();
    task.setId(123L);
    task.setSimulationId(UUID.randomUUID().toString());
    task.setInstanceId(UUID.randomUUID().toString());
    task.setIteration(300);
    task.setIterationCount(100);

    SimulationDataStore state = AntSimulationImpl.randomState();
    when(mockStorageService.getState(
        task.getSimulationId(), task.getInstanceId(), task.getIteration()))
            .thenReturn(new ByteArrayInputStream(toByteArray(state)));

    client.executeTask(task);

    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());

    byte[] newState = IOUtils.toByteArray(stateIn.getValue());
    assertTrue(newState.length > 0);

    AntSimulationState stateData =
        new AntSimulationState(MemorySimulationDataStore.fromByteArray(newState));
    assertTrue(getAntCount(stateData.getModel().getEnvironment().getEntities()) > 0);
  }

  private int getAntCount(Iterable<Entity> entities) {
    int count = 0;
    for(Entity entity : entities) {
      if(entity instanceof Ant) {
        count++;
      }
    }
    return count;
  }
}
