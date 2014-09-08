package org.mechaverse.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.mechaverse.common.MechaverseConfig;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.Task;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.api.SimulationService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Performs simulation tasks assigned by the mechaverse manager.
 */
public class MechaverseClient {

  private static final String DONE_MSG = "Done.";
  private static final String CONNECT_ERROR_MSG = "Connection failed.";
  private static final String FAILED_ERROR_MSG = "Failed.";

  private final SimulationService simulationService;
  private final MechaverseManager manager;
  private final MechaverseStorageService storageService;
  private final int instanceIdx;
  private AtomicBoolean running = new AtomicBoolean(true);

  public static void main(String[] args) throws Exception {
    try (ClassPathXmlApplicationContext context =
        new ClassPathXmlApplicationContext("spring/applicationContext.xml")) {
      final MechaverseConfig config = context.getBean(MechaverseConfig.class);

      SimulationService simulationService = config.getSimulationService();

      final List<MechaverseClient> clientInstances = new ArrayList<>();
      ExecutorService executorService =
          Executors.newFixedThreadPool(simulationService.getInstanceCount());
      for (int idx = 0; idx < simulationService.getInstanceCount(); idx++) {
        final int instanceIdx = idx;
        executorService.submit(new Runnable() {
          @Override
          public void run() {
            MechaverseClient clientInstance = new MechaverseClient(config.getSimulationService(),
              config.getManager(), config.getStorageService(), instanceIdx);
            clientInstances.add(clientInstance);
            clientInstance.start();
          }
        });
      }

      executorService.shutdown();
      while(!executorService.awaitTermination(10, TimeUnit.SECONDS)) {}
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
    while (running.get()) {
      try {
        Task task = getTask();
        if(task != null) {
          executeTask(task);
        } else {
          sleep();
        }
      } catch (Exception e) {
        sleep();
      }
    }
  }

  public void stop() {
    running.set(false);
  }

  public Task getTask() throws Exception {
    try {
      logOperationStart("Getting task");
      Task task = manager.getTask();
      if(task != null) {
        logOperationDone();
      } else {
        System.out.println("Nothing to do.");
      }
      return task;
    } catch(Throwable ex) {
      System.out.flush();
      printErrorMessage(ex);
      throw ex;
    }
  }

  public void executeTask(Task task) throws Exception {
    System.out.printf("(%d) Executing task [simulation: %s, instance: %s, iteration: %d]\n",
        instanceIdx, task.getSimulationId(), task.getInstanceId(), task.getIteration());

    try {
      byte[] state = null;
      if (task.getIteration() >= 0) {
        // Get the state from the storage service.
        logSubOperationStart("Retrieving simulation state");
        InputStream stateIn = storageService.getState(
            task.getSimulationId(), task.getInstanceId(), task.getIteration());
        state = IOUtils.toByteArray(stateIn);
        logOperationDone();
      } else {
        // Generate a new state.
        logSubOperationStart("Generating initial simulation state");
        state = simulationService.generateRandomState();
        logOperationDone();
      }

      if (task.getIterationCount() > 0) {
        logSubOperationStart("Performing " + task.getIterationCount() + " iterations");
        simulationService.setState(instanceIdx, state);
        simulationService.step(instanceIdx, task.getIterationCount());
        state = simulationService.getState(instanceIdx);
        logOperationDone();
      }

      // Submit result.
      logSubOperationStart("Submitting result");
      manager.submitResult(task.getId(), new ByteArrayInputStream(state));
      logOperationDone();
    } catch (Throwable ex) {
      printErrorMessage(ex);
      throw ex;
    }
  }

  private void logOperationStart(String msg) {
    System.out.printf("(%d) %s ... ", instanceIdx, msg);
  }

  private void logSubOperationStart(String msg) {
    System.out.printf("(%d)     %s ... ", instanceIdx, msg);
  }

  private void logOperationDone() {
    System.out.println(DONE_MSG);
  }

  private void sleep() {
    try {
      Thread.sleep(60 * 1000);
    } catch (InterruptedException e1) {}
  }

  private void printErrorMessage(Throwable ex) {
    if (ex.getCause() instanceof ConnectException) {
      System.out.println(CONNECT_ERROR_MSG);
    } else {
      System.out.println(FAILED_ERROR_MSG);
      ex.printStackTrace();
    }
  }
}
