package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.Nest;
import org.mechaverse.simulation.ant.api.util.EntityUtil;

public class CellEnvironment {

  // TODO(thorntonv): Handle multiple nests and nests of different types.

  private static final double[] DIRECTION_ANGLES = {
      0, Math.PI / 4, Math.PI / 2, 3 * Math.PI / 4, Math.PI, 5 * Math.PI / 4, 3 * Math.PI / 2,
      7 * Math.PI / 4};

  private final int rowCount;
  private final int colCount;
  private final Cell[][] cells;
  private final Environment env;

  private Direction[][] nestDirectionIndex;

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
      if (entity instanceof Nest) {
        Cell nestCell = getCell(entity);
        nestDirectionIndex = new Direction[rowCount][colCount];
        for (int row = 0; row < rowCount; row++) {
          for (int col = 0; col < colCount; col++) {
            nestDirectionIndex[row][col] = getDirection(cells[row][col], nestCell);
          }
        }
      }
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

  public int getDistance(Cell fromCell, Cell toCell) {
    return Math.abs(fromCell.getRow() - toCell.getRow())
        + Math.abs(fromCell.getColumn() - toCell.getColumn());
  }

  public Direction getDirection(Cell fromCell, Cell toCell) {
    // Reverse the order of subtraction to adjust for the graphical coordinate system.
    int deltaY = fromCell.getRow() - toCell.getRow();
    int deltaX = toCell.getColumn() - fromCell.getColumn();
    double angle = Math.atan2(deltaY, deltaX);
    angle = angle >= 0 ? angle : angle + 2 * Math.PI;


    Direction closestDirection = null;
    double closestDirectionAngleDelta = Double.MAX_VALUE;
    for (int ordinal = 0; ordinal < EntityUtil.DIRECTIONS.length; ordinal++) {
      double directionAngleDelta = Math.abs(DIRECTION_ANGLES[ordinal] - angle);
      if (closestDirection == null || directionAngleDelta < closestDirectionAngleDelta) {
        closestDirection = EntityUtil.DIRECTIONS[ordinal];
        closestDirectionAngleDelta = directionAngleDelta;
      }
    }

    return closestDirection;
  }

  public Direction getNestDirection(Cell fromCell) {
    return nestDirectionIndex[fromCell.getRow()][fromCell.getColumn()];
  }

  public void addEntity(Entity entity, Cell cell) {
    env.getEntities().add(entity);
    setEntityCell(entity, cell);
  }

  public void moveEntityToCell(EntityType entityType, Cell fromCell, Cell targetCell) {
    Entity entity = fromCell.removeEntity(entityType);
    targetCell.setEntity(entity, entityType);
  }

  public void updateModel() {
    env.getEntities().clear();
    for(int row = 0; row < cells.length; row++) {
      for(int col = 0; col < cells[row].length; col++) {
        env.getEntities().addAll(cells[row][col].getEntities());
      }
    }
  }

  private void setEntityCell(Entity entity, Cell cell) {
    entity.setX(cell.getColumn());
    entity.setY(cell.getRow());
    cell.setEntity(entity);
  }

  private boolean isValidCellCoordinate(int row, int column) {
    return row >= 0 && row < rowCount && column >= 0 && column < colCount;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int row = 0; row < cells.length; row++) {
      for (int col = 0; col < cells[row].length; col++) {
        for (EntityType type : EntityType.values()) {
          if (cells[row][col].hasEntity(type)) {
            sb.append(type.name().charAt(0));
          } else {
            sb.append("-");
          }
        }

        sb.append(" ");
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
