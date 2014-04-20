#include "CellEnvironment.h"

#include <stddef.h>

#include <core/simulation/InvalidSimulationStateException.h>

using namespace mechaverse;

CellEnvironment::CellEnvironment()
: rowCount(0), colCount(0), cells(NULL) {
}

CellEnvironment::~CellEnvironment() {
  freeCells();
}

void CellEnvironment::setEnvironment(Environment* env) {
  if (cells != NULL) {
    freeCells();
  }

  this->rowCount = env->height();
  this->colCount = env->width();

  // Allocate cells.
  cells = new Cell*[rowCount];
  for (int row = 0; row < rowCount; row++) {
    cells[row] = new Cell[colCount];
    for (int col = 0; col < colCount; col++) {
      cells[row][col] = Cell(row, col);
    }
  }

  // Add entities to the appropriate cells.

  for (int idx = 0; idx < env->ant_size(); idx++) {
    Ant* entity = env->mutable_ant(idx);
    getCell(entity->entity_state()).setAnt(entity);
  }
  for (int idx = 0; idx < env->barrier_size(); idx++) {
    Barrier* entity = env->mutable_barrier(idx);
    getCell(entity->entity_state()).setBarrier(entity);
  }
  for (int idx = 0; idx < env->conduit_size(); idx++) {
    Conduit* entity = env->mutable_conduit(idx);
    getCell(entity->entity_state()).setConduit(entity);
  }
  for (int idx = 0; idx < env->dirt_size(); idx++) {
    Dirt* entity = env->mutable_dirt(idx);
    getCell(entity->entity_state()).setDirt(entity);
  }
  for (int idx = 0; idx < env->food_size(); idx++) {
    Food* entity = env->mutable_food(idx);
    getCell(entity->entity_state()).setFood(entity);
  }
  for (int idx = 0; idx < env->pheromone_size(); idx++) {
    Pheromone* entity = env->mutable_pheromone(idx);
    getCell(entity->entity_state()).setPheromone(entity);
  }
  for (int idx = 0; idx < env->rock_size(); idx++) {
    Rock* entity = env->mutable_rock(idx);
    getCell(entity->entity_state()).setRock(entity);
  }
}

Coordinate CellEnvironment::coordinateOfCellInDirection(const Cell& cell, Direction direction) {
  Coordinate coordinate;
  coordinate.row = cell.getRow();
  coordinate.col = cell.getCol();
  switch(direction) {
    case EAST:
      coordinate.col++;
      break;
    case NORTH_EAST:
      coordinate.row--;
      coordinate.col++;
      break;
    case NORTH:
      coordinate.row--;
      break;
    case NORTH_WEST:
      coordinate.row--;
      coordinate.col--;
      break;
    case WEST:
      coordinate.col--;
      break;
    case SOUTH_WEST:
      coordinate.row++;
      coordinate.col--;
      break;
    case SOUTH:
      coordinate.row++;
      break;
    case SOUTH_EAST:
      coordinate.row++;
      coordinate.col++;
      break;
  }

  return coordinate;
}

bool CellEnvironment::hasCellInDirection(const Cell& cell, Direction direction) {
  Coordinate coordinate = coordinateOfCellInDirection(cell, direction);
  return coordinate.row >= 0 && coordinate.row < rowCount && coordinate.col >= 0
      && coordinate.col < colCount;
}

Cell& CellEnvironment::cellInDirection(const Cell& cell, Direction direction) {
  Coordinate coordinate = coordinateOfCellInDirection(cell, direction);
  return cells[coordinate.row][coordinate.col];
}

void CellEnvironment::freeCells() {
  if (cells != NULL) {
    for (int row = 0; row < rowCount; row++) {
      delete[] cells[row];
    }
    delete[] cells;

    rowCount = 0;
    colCount = 0;
    cells = NULL;
  }
}
