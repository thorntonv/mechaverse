package org.mechaverse.client;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.Task;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.api.SimulationService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
public class MechaverseClientTest {

  @Autowired private MechaverseManager mockManager;
  @Autowired private SimulationService mockSimulationService;
  @Autowired private MechaverseStorageService mockStorageService;

  private MechaverseClient client;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.reset(mockManager, mockSimulationService, mockStorageService);

    client = new MechaverseClient(mockSimulationService, mockManager, mockStorageService, 0);
  }

  @Test
  public void executeTask() throws Exception {
    MechaverseClient client =
        new MechaverseClient(mockSimulationService, mockManager, mockStorageService, 0);
    Task task = new Task();
    task.setId(123L);
    task.setSimulationId(UUID.randomUUID().toString());
    task.setInstanceId(UUID.randomUUID().toString());
    task.setIteration(300);
    task.setIterationCount(20);

    byte[] state = "state".getBytes();
    byte[] newState = "newState".getBytes();
    when(mockStorageService.getState(
        task.getSimulationId(), task.getInstanceId(), task.getIteration()))
            .thenReturn(new ByteArrayInputStream(state));

    when(mockSimulationService.getState(0)).thenReturn(newState);

    client.executeTask(task);

    verify(mockSimulationService).setState(0, state);
    verify(mockSimulationService).step(0, 20);
    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());
    assertArrayEquals(newState, IOUtils.readBytesFromStream(stateIn.getValue()));
  }

  @Test
  public void executeTask_initialState() throws Exception {
    Task task = new Task();
    task.setId(123L);
    task.setSimulationId(UUID.randomUUID().toString());
    task.setInstanceId(UUID.randomUUID().toString());
    task.setIteration(-1);
    task.setIterationCount(0);

    byte[] state = "state".getBytes();
    when(mockSimulationService.generateRandomState()).thenReturn(state);

    client.executeTask(task);

    verify(mockSimulationService, never()).setState(anyInt(), any(byte[].class));
    verify(mockSimulationService, never()).step(anyInt(), anyInt());
    ArgumentCaptor<InputStream> stateIn = ArgumentCaptor.forClass(InputStream.class);
    verify(mockManager).submitResult(eq(task.getId()), stateIn.capture());
    assertArrayEquals(state, IOUtils.readBytesFromStream(stateIn.getValue()));
  }
}
