package org.mechaverse.simulation.benchmark;

import java.util.Random;
import java.util.UUID;

import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.Rock;
import org.mechaverse.simulation.ant.api.model.SimulationState;
import org.mechaverse.simulation.ant.core.AntSimulationImpl;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;

public class AntSimulationBenchmark extends Benchmark {

  @Param(value = {"500"}) int numAnts;

  private AntSimulationImpl simulation = new AntSimulationImpl();
  private Random random = new Random();

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    simulation = newSimulation(numAnts);
  }

  public int timeUpdate(int reps) throws Exception {
    for (int i = 0; i < reps; i++) {
      simulation.step();
    }
    return 0;
  }

  public static void main(String[] args) throws InterruptedException {
    CaliperMain.main(AntSimulationBenchmark.class, args);
  }

  private AntSimulationImpl newSimulation(int numAnts) {
    AntSimulationImpl simulation = new AntSimulationImpl();

    SimulationState state = simulation.getState();
    for (int cnt = 1; cnt < numAnts; cnt++) {

      boolean added = false;
      while (!added) {
        int row = random.nextInt(state.getEnvironment().getHeight());
        int col = random.nextInt(state.getEnvironment().getWidth());

        boolean occupied = false;
        for (Entity otherEntity : state.getEnvironment().getEntities()) {
          if (otherEntity.getY() == row && otherEntity.getX() == col) {
            occupied = true;
            break;
          }
        }
        if (!occupied) {
          Ant ant = new Ant();
          ant.setId(UUID.randomUUID().toString());
          ant.setX(col);
          ant.setY(row);
          ant.setDirection(Direction.NORTH);
          ant.setEnergy(100);
          ant.setMaxEnergy(100);
          ant.setCarriedEntity(new Rock());
          state.getEnvironment().getEntities().add(ant);
          added = true;
        }
      }
    }
    simulation.setState(state);
    return simulation;
  }
}
