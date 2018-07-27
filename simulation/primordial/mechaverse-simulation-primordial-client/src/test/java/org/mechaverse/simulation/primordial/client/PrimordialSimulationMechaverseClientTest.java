package org.mechaverse.simulation.primordial.client;

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
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.mechaverse.simulation.primordial.core.util.PrimordialSimulationModelUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link PrimordialSimulationMechaverseClient}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
public class PrimordialSimulationMechaverseClientTest {

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

    PrimordialSimulationModel state = new PrimordialSimulationModel();
    when(mockStorageService.getState(
        task.getSimulationId(), task.getInstanceId(), task.getIteration()))
            .thenReturn(new ByteArrayInputStream(PrimordialSimulationModelUtil.serialize(state)));

    client.executeTask(task);

    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());

    byte[] newState = IOUtils.toByteArray(stateIn.getValue());
    assertTrue(newState.length > 0);

    assertTrue(getEntityCount(state.getEnvironment().getEntities()) > 0);
  }

  private int getEntityCount(Iterable<EntityModel<EntityType>> entities) {
    int count = 0;
    for(EntityModel entity : entities) {
      count++;
    }
    return count;
  }
}
