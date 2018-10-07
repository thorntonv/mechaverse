package org.mechaverse.client;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mechaverse.manager.api.MechaverseManagerApi;
import org.mechaverse.manager.api.model.SimulationInfo;
import org.mechaverse.manager.api.model.Task;
import org.mechaverse.simulation.common.Simulation;
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

  private final String clientId;
  private final MechaverseManagerApi manager;
  private final int instanceIdx;
  private AtomicBoolean running = new AtomicBoolean(true);

  public MechaverseClient(String clientId, MechaverseManagerApi manager, int instanceIdx) {
    this.clientId = clientId;
    this.manager = manager;
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
      Task task = manager.getTask(clientId);
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

    SimulationInfo simulationInfo = manager.getSimulationInfo(task.getSimulationId());
    try(AbstractApplicationContext ctx = getApplicationContext(simulationInfo.getConfig().getSimulationType())) {
      Simulation simulation = createSimulation(ctx);
      if (task.getIteration() >= 0) {
        // Get the state from the storage service.
        logSubOperationStart("Retrieving simulation state");
        byte[] stateData = manager.getState(
            task.getSimulationId(), task.getInstanceId(), task.getIteration());
        simulation.setStateData(stateData);

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

      synchronized (MechaverseClient.class) {
        logSubOperationStart("Serializing simulation state");
        byte[] stateData = simulation.getStateData();
        logOperationDone();

        // Submit result.
        logSubOperationStart("Submitting result");
        manager.submitResult(task.getId(), stateData);
        logOperationDone();
      }

    } catch (Throwable ex) {
      printErrorMessage(ex);
      throw ex;
    }
  }

  protected AbstractApplicationContext getApplicationContext(String simulationType) {
    return new ClassPathXmlApplicationContext(simulationType + "-simulation-context.xml");
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
