#ifndef CELLENVIRONMENT_H_
#define CELLENVIRONMENT_H_

#include <core/simulation/ant/ant-simulation.pb.h>
#include <core/simulation/ant/Cell.h>

namespace mechaverse {

struct Coordinate {
  int row;
  int col;
};

/**
 * An environment which supports efficient cell related operations.
 */
class CellEnvironment {

 private:

  int rowCount;
  int colCount;
  Cell** cells;

 public:

  CellEnvironment();
  virtual ~CellEnvironment();

  /**
   * Initialize the cells of this environment based on the given environment.
   */
  void setEnvironment(Environment* env);

  /**
   * @return the cell at the given row and column.
   */
  inline Cell& getCell(int row, int col) const {
    return cells[row][col];
  }

  /**
   * @return the cell which contains the given entity.
   */
  inline Cell& getCell(EntityState* entityState) const {
    return cells[entityState->y()][entityState->x()];
  }

  /**
   * @return the cell which contains the given entity.
   */
  inline Cell& getCell(const EntityState& entityState) const {
    return cells[entityState.y()][entityState.x()];
  }

  /**
   * @return true if a cell exists in the given direction relative to the given cell.
   */
  bool hasCellInDirection(const Cell& cell, Direction direction);

  /**
   * @return the cell in the given direction relative to the given cell.
   */
  Cell& cellInDirection(const Cell& cell, Direction direction);

  /**
   * @return the coordinate of the cell in the given direction relative to the given cell.
   */
  Coordinate coordinateOfCellInDirection(const Cell& cell, Direction direction);

 private:

  void freeCells();
};

}

#endif /* CELLENVIRONMENT_H_ */
