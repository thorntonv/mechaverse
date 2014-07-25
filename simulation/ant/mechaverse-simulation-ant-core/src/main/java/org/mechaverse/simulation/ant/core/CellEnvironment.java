package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Environment;

public class CellEnvironment {

  private final int rowCount;
  private final int colCount;
  private final Cell[][] cells;
  private final Environment env;

  public CellEnvironment(Environment env) {
    this.rowCount = env.getHeight();
    this.colCount = env.getWidth();
    this.cells = new Cell[env.getHeight()][env.getWidth()];
    this.env = env;

    // Allocate cells.
    for (int row = 0; row < env.getHeight(); row++) {
      for (int col = 0; col < env.getWidth(); col++) {
        cells[row][col] = new Cell(row, col);
      }
    }

    // Add entities to the appropriate cells.
    for (Entity entity : env.getEntities()) {
      setEntityCell(entity, getCell(entity));
    }
  }

  public Environment getEnvironment() {
    updateModel();
    return env;
  }

  public int getRowCount() {
    return rowCount;
  }

  public int getColumnCount() {
    return colCount;
  }

  public boolean hasCell(int row, int col) {
    return row >= 0 && col >= 0 && row < cells.length && col < cells[row].length;
  }

  public Cell getCell(int row, int col) {
    return cells[row][col];
  }

  public Cell getCell(Entity entity) {
    return cells[entity.getY()][entity.getX()];
  }

  public Cell getCellInDirection(Cell cell, Direction direction) {
    int row = cell.getRow();
    int col = cell.getColumn();
    switch(direction) {
      case EAST:
        col++;
        break;
      case NORTH_EAST:
        row--;
        col++;
        break;
      case NORTH:
        row--;
        break;
      case NORTH_WEST:
        row--;
        col--;
        break;
      case WEST:
        col--;
        break;
      case SOUTH_WEST:
        row++;
        col--;
        break;
      case SOUTH:
        row++;
        break;
      case SOUTH_EAST:
        row++;
        col++;
        break;
    }
    return isValidCellCoordinate(row, col) ? cells[row][col] : null;
  }

  public void addEntity(Entity entity, Cell cell) {
    env.getEntities().add(entity);
    setEntityCell(entity, cell);
  }

  public void moveEntityToCell(EntityType entityType, Cell fromCell, Cell targetCell) {
    Entity entity = fromCell.removeEntity(entityType);
    entity.setX(targetCell.getColumn());
    entity.setY(targetCell.getRow());
    targetCell.setEntity(entity, entityType);
  }

  private void setEntityCell(Entity entity, Cell cell) {
    entity.setX(cell.getColumn());
    entity.setY(cell.getRow());
    cell.setEntity(entity);
  }

  private boolean isValidCellCoordinate(int row, int column) {
    return row >= 0 && row < rowCount && column >= 0 && column < colCount;
  }

  public void updateModel() {
    env.getEntities().clear();
    for(int row = 0; row < cells.length; row++) {
      for(int col = 0; col < cells[row].length; col++) {
        env.getEntities().addAll(cells[row][col].getEntities());
      }
    }
  }
}
