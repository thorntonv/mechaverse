package org.mechaverse.simulation.ant.core;

import java.util.Random;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.Rock;
import org.mechaverse.simulation.ant.api.model.SimulationState;

/**
 * Unit test for {@link AntSimulationImpl}.
 */
public class AntSimulationImplTest {

  private Random random;

  @Before
  public void setUp() {
    random = new Random(AntSimulationImplTest.class.getName().hashCode());
  }

  @Test
  public void simulate() {
    AntSimulationImpl simulation = newSimulation(500);

    for (int cnt = 0; cnt < 1000; cnt++) {
      simulation.step();
    }
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
