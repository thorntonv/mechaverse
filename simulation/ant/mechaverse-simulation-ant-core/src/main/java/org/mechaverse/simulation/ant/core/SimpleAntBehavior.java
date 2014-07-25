package org.mechaverse.simulation.ant.core;

import java.util.Random;

import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.core.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.ant.core.AntOutput.MoveDirection;
import org.mechaverse.simulation.ant.core.AntOutput.TurnDirection;

/**
 * An ant behavior that is based on a simple algorithm.
 */
public final class SimpleAntBehavior implements AntBehavior {

  private AntInput input;
  private final AntOutput output = new AntOutput();
  private final Random random = new Random();

  private boolean ignoreFood = false;

  @Override
  public void setInput(AntInput input) {
    this.input = input;
  }

  @Override
  public AntOutput getOutput() {
    EntityType cellEntityType = input.getCellSensor();

    if (input.getCarriedEntityType() == EntityType.NONE) {
      if (cellEntityType == EntityType.FOOD && !ignoreFood) {
        output.setPickUp(true);
        output.setDrop(false);
        return output;
      }
    } else {
      if (random.nextInt(100) < 10) {
        output.setDrop(true);
        output.setPickUp(false);
        ignoreFood = true;
        return output;
      }
    }

    if(input.getPheromoneType() == 0) {
      output.setLeavePheromone(4);
      return output;
    } else {
      output.setLeavePheromone(0);
    }
    EntityType frontEntityType = input.getFrontSensor().getEntityType();
    if (frontEntityType == null || frontEntityType == EntityType.ANT
        || frontEntityType == EntityType.ROCK || frontEntityType == EntityType.BARRIER) {
      output.setMoveDirection(MoveDirection.NONE);
      if (random.nextInt(2) == 0) {
        output.setTurnDirection(TurnDirection.COUNTERCLOCKWISE);
      } else {
        output.setTurnDirection(TurnDirection.CLOCKWISE);
      }
    } else {
      output.setMoveDirection(MoveDirection.FORWARD);
      output.setTurnDirection(TurnDirection.NONE);
      ignoreFood = false;
    }
    return output;
  }
}
