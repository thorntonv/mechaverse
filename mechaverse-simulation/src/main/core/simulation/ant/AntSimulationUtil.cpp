#include "AntSimulationUtil.h"

#include <stddef.h>

using namespace mechaverse;

AntSimulationUtil::AntSimulationUtil() {}

AntSimulationUtil::~AntSimulationUtil() {}

bool AntSimulationUtil::canMoveAntToCell(const Cell& cell) {
  // An an can move to the cell if it does not contain another ant, a barrier, a rock, or dirt.
  return cell.getAnt() == NULL && cell.getBarrier() == NULL && cell.getDirt() == NULL
      && cell.getRock() == NULL;
}

Direction AntSimulationUtil::directionCW(const Direction& direction) {
  switch(direction) {
    case EAST:
      return SOUTH_EAST;
    case NORTH_EAST:
      return EAST;
    case NORTH:
      return NORTH_EAST;
    case NORTH_WEST:
      return NORTH;
    case WEST:
      return NORTH_WEST;
    case SOUTH_WEST:
      return WEST;
    case SOUTH:
      return SOUTH_WEST;
    case SOUTH_EAST:
      return SOUTH;
  }
  return direction;
}

Direction AntSimulationUtil::directionCCW(const Direction& direction) {
  switch(direction) {
    case EAST:
      return NORTH_EAST;
    case NORTH_EAST:
      return NORTH;
    case NORTH:
      return NORTH_WEST;
    case NORTH_WEST:
      return WEST;
    case WEST:
      return SOUTH_WEST;
    case SOUTH_WEST:
      return SOUTH;
    case SOUTH:
      return SOUTH_EAST;
    case SOUTH_EAST:
      return EAST;
  }
  return direction;
}
