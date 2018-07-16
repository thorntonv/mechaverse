package org.mechaverse.simulation.common.cellautomaton.environment;

import java.util.ArrayList;
import java.util.List;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.util.SimulationModelUtil;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractCellEnvironmentModel<
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>,
    C extends AbstractCellModel<ENT_MODEL, ENT_TYPE>> extends
    EnvironmentModel<ENT_MODEL, ENT_TYPE> {

  private static final double[] DIRECTION_ANGLES = {
      0, Math.PI / 4, Math.PI / 2, 3 * Math.PI / 4, Math.PI, 5 * Math.PI / 4, 3 * Math.PI / 2,
      7 * Math.PI / 4};

  private C[][] cells;

  public boolean hasCell(int row, int col) {
    if(cells == null) {
      initCells();
    }
    return row >= 0 && col >= 0 && row < cells.length && col < cells[row].length;
  }

  public C getCell(int row, int col) {
    return getCells()[row][col];
  }

  public C getCell(EntityModel entity) {
    return getCells()[entity.getY()][entity.getX()];
  }

  public C getCellInDirection(C cell, Direction direction) {
    int row = cell.getRow();
    int col = cell.getColumn();
    switch (direction) {
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
    return isValidCellCoordinate(row, col) ? getCells()[row][col] : null;
  }

  public int getDistance(C fromCell, C toCell) {
    return Math.abs(fromCell.getRow() - toCell.getRow())
        + Math.abs(fromCell.getColumn() - toCell.getColumn());
  }

  public Direction getDirection(C fromCell, C toCell) {
    // Reverse the order of subtraction to adjust for the graphical coordinate system.
    int deltaY = fromCell.getRow() - toCell.getRow();
    int deltaX = toCell.getColumn() - fromCell.getColumn();
    double angle = Math.atan2(deltaY, deltaX);
    angle = angle >= 0 ? angle : angle + 2 * Math.PI;

    Direction closestDirection = null;
    double closestDirectionAngleDelta = Double.MAX_VALUE;
    for (int ordinal = 0; ordinal < SimulationModelUtil.DIRECTIONS.length; ordinal++) {
      double directionAngleDelta = Math.abs(DIRECTION_ANGLES[ordinal] - angle);
      if (closestDirection == null || directionAngleDelta < closestDirectionAngleDelta) {
        closestDirection = SimulationModelUtil.DIRECTIONS[ordinal];
        closestDirectionAngleDelta = directionAngleDelta;
      }
    }

    return closestDirection;
  }

  public void addEntity(ENT_MODEL entity, C cell) {
    setEntityCell(entity, cell);
  }

  public void moveEntityToCell(ENT_TYPE entityType, C fromCell, C targetCell) {
    ENT_MODEL entity = fromCell.removeEntity(entityType);
    targetCell.setEntity(entity);
  }

  @Override
  public void setWidth(int value) {
    super.setWidth(value);
  }

  @Override
  public void setHeight(int value) {
    super.setHeight(value);
  }

  public List<ENT_MODEL> getEntities() {
    List<ENT_MODEL> entities = new ArrayList<>();
    for (C[] row : getCells()) {
      for (C cell : row) {
        entities.addAll(cell.getEntities());
      }
    }
    return entities;
  }

  protected abstract C[][] createCells();

  protected abstract C createCell(int row, int column);

  protected C[][] getCells() {
    if (cells == null) {
      initCells();
    }
    return cells;
  }

  private void initCells() {
    this.cells = createCells();

    // Allocate cells.
    for (int row = 0; row < getHeight(); row++) {
      for (int col = 0; col < getWidth(); col++) {
        cells[row][col] = createCell(row, col);
      }
    }

    // Add entities to the appropriate cells.
    for (ENT_MODEL entityModel : super.getEntities()) {
      setEntityCell(entityModel, getCell(entityModel));
    }
  }

  private void setEntityCell(ENT_MODEL entityModel, C cell) {
    entityModel.setX(cell.getColumn());
    entityModel.setY(cell.getRow());
    cell.setEntity(entityModel);
  }

  private boolean isValidCellCoordinate(int row, int column) {
    return row >= 0 && row < getHeight() && column >= 0 && column < getWidth();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (C[] row : getCells()) {
      for (C cell : row) {
        for (ENT_TYPE type : getEntityTypes()) {
          if (cell.hasEntity(type)) {
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
