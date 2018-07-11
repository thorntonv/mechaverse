package org.mechaverse.simulation.ant.core.entity.ant;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.common.model.MoveDirection;
import org.mechaverse.simulation.common.model.TurnDirection;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mechaverse.simulation.common.util.SimulationUtil;

/**
 * An ant behavior that is based on a simple algorithm.
 */
public class SimpleAntBehavior implements AntBehavior {

  private AntInput input;
  private final AntOutput output = new AntOutput();

  private TurnDirection turnDirection = TurnDirection.NONE;
  private boolean leavePheromone = false;

  @Override
  public void setEntity(Ant entity) {}

  @Override
  public void setInput(AntInput input, RandomGenerator random) {
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
        output.setPickUpOrDrop(true);
        return output;
      } else if (input.getFrontSensor().getEntityType() == EntityType.FOOD
          && RandomUtil.nextEvent(.9, random)) {
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
        output.setPickUpOrDrop(true);
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

    if(input.getCarriedEntityType() == EntityType.FOOD && turnDirection == TurnDirection.NONE) {
      // Attempt to return to the nest.
      TurnDirection turnDirectionToNest =
          getTurnDirection(input.getDirection(), input.getNestDirection());
      if(turnDirectionToNest != TurnDirection.NONE && RandomUtil.nextEvent(.5, random)) {
        output.setTurnDirection(turnDirectionToNest);
        return output;
      }
    }

    turnDirection = TurnDirection.NONE;
    if (RandomUtil.nextEvent(.2, random)) {
      turnDirection = RandomUtil.nextEvent(.5, random)
          ? TurnDirection.COUNTERCLOCKWISE : TurnDirection.CLOCKWISE;

      output.setTurnDirection(turnDirection);
      return output;
    }

    // Occasionally drop a rock that is being carried.
    if(input.getCarriedEntityType() == EntityType.ROCK && RandomUtil.nextEvent(.1, random)) {
      output.setPickUpOrDrop(true);
      return output;
    }

    if(leavePheromone && input.getCellSensor() != EntityType.PHEROMONE) {
      output.setLeavePheromone(true);
      output.setPheromoneType(1);
      leavePheromone = false;
      return output;
    }

    output.setMoveDirection(MoveDirection.FORWARD);
    if (input.getCarriedEntityType() == EntityType.FOOD) {
      leavePheromone = true;
    }
    return output;
  }

  @Override
  public void onRemoveEntity() {}

  @Override
  public void setState(AntSimulationState state) {}

  @Override
  public void updateState(AntSimulationState state) {}

  private TurnDirection getTurnDirection(Direction currentDirection, Direction targetDirection) {
    if (currentDirection == targetDirection) {
      return TurnDirection.NONE;
    }
    int cwCount = 0;
    Direction direction = currentDirection;
    while (direction != targetDirection) {
      direction = SimulationUtil.directionCW(direction);
      cwCount++;
    }
    int ccwCount = 0;
    direction = currentDirection;
    while (direction != targetDirection) {
      direction = SimulationUtil.directionCCW(direction);
      ccwCount++;
    }
    if (cwCount < ccwCount) {
      return TurnDirection.CLOCKWISE;
    } else {
      return TurnDirection.COUNTERCLOCKWISE;
    }
  }
}
