package org.mechaverse.simulation.common.ui;

import com.rits.cloning.Cloner;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.tuple.Pair;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public class SimulationRendererPipeline<SIM_MODEL extends
    SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
    ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>> implements AutoCloseable {

  private static final Cloner cloner = new Cloner();

  static {
    cloner.registerImmutable(Map.class);
  }

  private class SimulationUpdateTask implements Runnable {

    @Override
    public void run() {
      while (running.get()) {
        try {
          simulation.step(1);
          final SIM_MODEL state = cloner.deepClone(simulation.getState());
          stateImageQueue.put(executorService.submit(() -> {
            Map<String, BufferedImage> environmentImages = new ConcurrentHashMap<>();
            state.getEnvironments().parallelStream().forEach(env ->
                environmentImages.put(env.getId(),
                    renderer.draw(state, state.getEnvironment(env.getId()))));
            return Pair.of(state, environmentImages);
          }));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private ExecutorService executorService = Executors
      .newFixedThreadPool(1);

  private BlockingQueue<Future<Pair<SIM_MODEL, Map<String, BufferedImage>>>> stateImageQueue = new ArrayBlockingQueue<>(
      60);

  private AtomicBoolean running = new AtomicBoolean(false);

  private final Simulation<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> simulation;

  private final SimulationRenderer<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> renderer;

  public SimulationRendererPipeline(
      Simulation<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> simulation,
      SimulationRenderer<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> renderer) {
    this.simulation = simulation;
    this.renderer = renderer;
  }

  public void start() {
    if (running.compareAndSet(false, true)) {
      Thread thread = new Thread(new SimulationUpdateTask());
      thread.start();
    }
  }

  public void stop() {
    running.set(false);
  }

  public Pair<SIM_MODEL, Map<String, BufferedImage>> getNextStateImage()
      throws InterruptedException, ExecutionException {
    Future<Pair<SIM_MODEL, Map<String, BufferedImage>>> result = stateImageQueue.poll();
    if (result != null) {
      return result.get();
    }
    boolean wasRunning = running.get();
    if (!wasRunning) {
      start();
    }
    result = stateImageQueue.take();
    if (!wasRunning) {
      stop();
    }
    return result.get();
  }

  public SimulationRenderer<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> getRenderer() {
    return renderer;
  }

  @Override
  public void close() throws Exception {
    stop();
    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS);
  }
}
