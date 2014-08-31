package org.mechaverse.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.Task;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.api.SimulationService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MechaverseClient {

  private final SimulationService simulationService;
  private final MechaverseManager manager;
  private final MechaverseStorageService storageService;
  private final int instanceIdx;

  public static void main(String[] args) {
    try (ClassPathXmlApplicationContext context =
        new ClassPathXmlApplicationContext("spring/applicationContext.xml")) {
      MechaverseClientConfig config = context.getBean(MechaverseClientConfig.class);

      // TODO(thorntonv): Create a thread for each instance supported by the simulation service.
      new MechaverseClient(config.getSimulationService(), config.getManager(),
        config.getStorageService(), 0).start();
    }
  }

  public MechaverseClient(SimulationService simulationService, MechaverseManager manager,
      MechaverseStorageService storageService, int instanceIdx) {
    this.simulationService = simulationService;
    this.manager = manager;
    this.storageService = storageService;
    this.instanceIdx = instanceIdx;
  }

  public void start() {
  }

  public Task getTask() throws Exception {
    return manager.getTask();
  }

  public void executeTask(Task task) throws Exception {
    byte[] state = null;
    if (task.getIteration() >= 0) {
      // Get the state from the storage service.
      InputStream stateIn = storageService.getState(
          task.getSimulationId(), task.getInstanceId(), task.getIteration());
      state = IOUtils.readBytesFromStream(stateIn);
    } else {
      // Generate a new state.
      state = simulationService.generateRandomState();
    }

    if (task.getIterationCount() > 0) {
      simulationService.setState(instanceIdx, state);
      simulationService.step(instanceIdx, task.getIterationCount());
      state = simulationService.getState(instanceIdx);
    }

    // Submit result.
    manager.submitResult(task.getId(), new ByteArrayInputStream(state));
  }
}
