package org.mechaverse.simulation.ant.core.model;

import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellEnvironmentModel;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;

public class CellEnvironment extends
    AbstractCellEnvironmentModel<EntityModel<EntityType>, EntityType, Cell> {

  private Direction[][] nestDirectionIndex;

  public CellEnvironment() {
    for (EntityModel entity : getEntities()) {
      if (entity instanceof Nest) {
        Cell nestCell = getCell(entity);
        nestDirectionIndex = new Direction[getHeight()][getWidth()];
        for (int row = 0; row < getHeight(); row++) {
          for (int col = 0; col < getWidth(); col++) {
            nestDirectionIndex[row][col] = getDirection(getCells()[row][col], nestCell);
          }
        }
      }
    }
  }

  @Override
  protected Cell[][] createCells() {
    return new Cell[getHeight()][getWidth()];
  }

  @Override
  protected Cell createCell(final int row, final int column) {
    return new Cell(row, column);
  }

  public Direction getNestDirection(Cell fromCell) {
    return nestDirectionIndex[fromCell.getRow()][fromCell.getColumn()];
  }

  @Override
  public EntityType[] getEntityTypes() {
    return new EntityType[0];
  }
}
