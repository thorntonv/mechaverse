package org.mechaverse.client;

import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mechaverse.common.MechaverseConfig;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.Task;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStoreInputStream;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Performs simulation tasks assigned by the mechaverse manager.
 */
public class MechaverseClient {

  private static final String DONE_MSG = "Done.";
  private static final String CONNECT_ERROR_MSG = "Connection failed.";
  private static final String FAILED_ERROR_MSG = "Failed.";

  private final MechaverseManager manager;
  private final MechaverseStorageService storageService;
  private final int instanceIdx;
  private AtomicBoolean running = new AtomicBoolean(true);

  public static void main(String[] args) throws Exception {
    try (ClassPathXmlApplicationContext context =
        new ClassPathXmlApplicationContext("spring/applicationContext.xml")) {
      final MechaverseConfig config = context.getBean(MechaverseConfig.class);

      final List<MechaverseClient> clientInstances = new ArrayList<>();
      int instanceCount = 1;
      ExecutorService executorService = Executors.newFixedThreadPool(instanceCount);
      for (int idx = 0; idx < instanceCount; idx++) {
        final int instanceIdx = idx;
        executorService.submit(() -> {
          MechaverseClient clientInstance = new MechaverseClient(
            config.getManager(), config.getStorageService(), instanceIdx);
          clientInstances.add(clientInstance);
          clientInstance.start();
        });
      }

      executorService.shutdown();
      while(!executorService.awaitTermination(10, TimeUnit.SECONDS)) {}
    }
  }

  public MechaverseClient(MechaverseManager manager,
      MechaverseStorageService storageService, int instanceIdx) {
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

  public Task getTask() {
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

    try(AbstractApplicationContext ctx = getApplicationContext()) {
      Simulation simulation = createSimulation(ctx);
      if (task.getIteration() >= 0) {
        // Get the state from the storage service.
        logSubOperationStart("Retrieving simulation state");
        InputStream in = storageService.getState(
            task.getSimulationId(), task.getInstanceId(), task.getIteration());

        SimulationDataStore simulationDataStore =
            new SimulationDataStoreInputStream(in, MemorySimulationDataStore::new).readDataStore();
        simulation.setStateData(simulationDataStore.get(SimulationDataStore.STATE_KEY));

        logOperationDone();
      } else {
        // Generate a new state.
        logSubOperationStart("Generating initial simulation state");
        simulation.setState(simulation.generateRandomState());
        logOperationDone();
      }

      if (task.getIterationCount() > 0) {
        logSubOperationStart("Performing " + task.getIterationCount() + " iterations");
        long startTime = System.nanoTime();
        simulation.step(task.getIterationCount());
        long runTime =
            TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        logOperationDone(runTime);
      }

      // Submit result.
      logSubOperationStart("Submitting result");

      byte[] stateData = simulation.getStateData();
      MemorySimulationDataStore simulationDataStore = new MemorySimulationDataStore();
      simulationDataStore.put(SimulationDataStore.STATE_KEY, stateData);
      manager.submitResult(task.getId(), SimulationDataStoreInputStream.newInputStream(simulationDataStore));
      logOperationDone();
    } catch (Throwable ex) {
      printErrorMessage(ex);
      throw ex;
    }
  }

  protected AbstractApplicationContext getApplicationContext() {
    return new ClassPathXmlApplicationContext("simulation-context.xml");
  }

  protected Simulation createSimulation(ApplicationContext ctx) {
    return ctx.getBean(Simulation.class);
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

  private void logOperationDone(long runTimeMillis) {
    if(runTimeMillis < 1000) {
      System.out.println(DONE_MSG + " Completed in " + runTimeMillis + " ms.");
    } else {
      System.out.printf(DONE_MSG + " Completed in %.2f sec.%n", runTimeMillis / 1000.0f);
    }
  }

  private void sleep() {
    try {
      Thread.sleep(60 * 1000);
    } catch (InterruptedException ignored) {}
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
