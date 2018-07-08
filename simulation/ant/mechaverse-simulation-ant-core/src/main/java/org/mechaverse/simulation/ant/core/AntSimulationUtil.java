package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.common.util.SimulationModelUtil;

public final class AntSimulationUtil {

  private AntSimulationUtil() {}

  public static void turnCW(Entity entity) {
    entity.setDirection(directionCW(entity.getDirection()));
  }

  public static void turnCCW(Entity entity) {
    entity.setDirection(directionCCW(entity.getDirection()));
  }

  public static Direction directionCW(Direction direction) {
    switch (direction) {
      case EAST:
        return Direction.SOUTH_EAST;
      case NORTH_EAST:
        return Direction.EAST;
      case NORTH:
        return Direction.NORTH_EAST;
      case NORTH_WEST:
        return Direction.NORTH;
      case WEST:
        return Direction.NORTH_WEST;
      case SOUTH_WEST:
        return Direction.WEST;
      case SOUTH:
        return Direction.SOUTH_WEST;
      case SOUTH_EAST:
        return Direction.SOUTH;
    }
    return direction;
  }

  public static Direction directionCCW(Direction direction) {
    switch (direction) {
      case EAST:
        return Direction.NORTH_EAST;
      case NORTH_EAST:
        return Direction.NORTH;
      case NORTH:
        return Direction.NORTH_WEST;
      case NORTH_WEST:
        return Direction.WEST;
      case WEST:
        return Direction.SOUTH_WEST;
      case SOUTH_WEST:
        return Direction.SOUTH;
      case SOUTH:
        return Direction.SOUTH_EAST;
      case SOUTH_EAST:
        return Direction.EAST;
    }
    return direction;
  }

  public static Direction oppositeDirection(Direction direction) {
    switch (direction) {
      case EAST:
        return Direction.WEST;
      case NORTH_EAST:
        return Direction.SOUTH_WEST;
      case NORTH:
        return Direction.SOUTH;
      case NORTH_WEST:
        return Direction.SOUTH_EAST;
      case WEST:
        return Direction.EAST;
      case SOUTH_WEST:
        return Direction.NORTH_EAST;
      case SOUTH:
        return Direction.NORTH;
      case SOUTH_EAST:
        return Direction.SOUTH_WEST;
    }
    throw new IllegalArgumentException("Invalid direction " + direction.name());
  }

  public static Direction randomDirection(RandomGenerator random) {
    return SimulationModelUtil.DIRECTIONS[random.nextInt(SimulationModelUtil.DIRECTIONS.length)];
  }
}
