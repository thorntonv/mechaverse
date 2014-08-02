package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.core.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.ant.core.AntOutput.MoveDirection;
import org.mechaverse.simulation.ant.core.AntOutput.TurnDirection;
import org.mechaverse.simulation.common.util.RandomUtil;

/**
 * An ant behavior that is based on a simple algorithm.
 */
public class SimpleAntBehavior implements AntBehavior {

  private AntInput input;
  private final AntOutput output = new AntOutput();

  private TurnDirection turnDirection = TurnDirection.NONE;

  @Override
  public void setInput(AntInput input) {
    this.input = input;
  }

  @Override
  public AntOutput getOutput(RandomGenerator random) {
    EntityType cellEntityType = input.getCellSensor();

    output.resetToDefault();

    if (input.getEnergyLevel() <= 4) {
      // Consume food if available.
      if (input.getCarriedEntityType() == EntityType.FOOD || cellEntityType == EntityType.FOOD) {
        output.setConsume(true);
        return output;
      }
    }

    if (input.getCarriedEntityType() == EntityType.NONE) {
      // Look for nearby food.
      if (cellEntityType == EntityType.FOOD) {
        output.setPickUp(true);
        return output;
      } else if (input.getFrontSensor().getEntityType() == EntityType.FOOD) {
        output.setMoveDirection(MoveDirection.FORWARD);
        return output;
      } else if (input.getFrontLeftSensor().getEntityType() == EntityType.FOOD
          || input.getLeftSensor().getEntityType() == EntityType.FOOD) {
        output.setTurnDirection(TurnDirection.COUNTERCLOCKWISE);
        return output;
      } else if (input.getFrontRightSensor().getEntityType() == EntityType.FOOD
          || input.getRightSensor().getEntityType() == EntityType.FOOD) {
        output.setTurnDirection(TurnDirection.CLOCKWISE);
        return output;
      }
    }

    EntityType frontEntityType = input.getFrontSensor().getEntityType();

    if (frontEntityType == EntityType.ROCK) {
      if(input.getCarriedEntityType() == EntityType.NONE && RandomUtil.nextEvent(.25, random)) {
        output.setPickUp(true);
        return output;
      }
    }

    if (frontEntityType == null || frontEntityType == EntityType.ANT
        || frontEntityType == EntityType.ROCK || frontEntityType == EntityType.BARRIER) {
      // Turn until an empty cell is found.
      if(turnDirection == TurnDirection.NONE) {
        turnDirection = RandomUtil.nextEvent(.5, random)
            ? TurnDirection.COUNTERCLOCKWISE : TurnDirection.CLOCKWISE;
      }

      output.setTurnDirection(turnDirection);
      return output;
    }

    turnDirection = TurnDirection.NONE;

    if(RandomUtil.nextEvent(.1, random)) {
      turnDirection = RandomUtil.nextEvent(.5, random)
          ? TurnDirection.COUNTERCLOCKWISE : TurnDirection.CLOCKWISE;

      output.setTurnDirection(turnDirection);
      return output;
    }

    // Occasionally drop a rock that is being carried.
    if(input.getCarriedEntityType() == EntityType.ROCK && RandomUtil.nextEvent(.1, random)) {
      output.setDrop(true);
      return output;
    }

    output.setMoveDirection(MoveDirection.FORWARD);

    return output;
  }

  @Override
  public void updateModel() {}
}
