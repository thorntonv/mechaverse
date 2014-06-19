package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Entity;


public final class AntSimulationUtil {

  private AntSimulationUtil() {}

  public static boolean canMoveAntToCell(Cell cell) {
    // An an can move to the cell if it does not contain another ant, a barrier, a rock, or dirt.
    return cell.getAnt() == null && cell.getBarrier() == null && cell.getDirt() == null
        && cell.getRock() == null;
  }

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
}
