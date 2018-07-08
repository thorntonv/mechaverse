package org.mechaverse.simulation.common.cellautomaton.environment;

import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.util.SimulationModelUtil;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractCellEnvironment<T extends Enum<T>, C extends AbstractCell<T>> {

    private static final double[] DIRECTION_ANGLES = {
            0, Math.PI / 4, Math.PI / 2, 3 * Math.PI / 4, Math.PI, 5 * Math.PI / 4, 3 * Math.PI / 2,
            7 * Math.PI / 4};

    private final int rowCount;
    private final int colCount;
    protected final C[][] cells;
    protected final EnvironmentModel env;
    protected final EntityFactory<T> entityFactory;

    public AbstractCellEnvironment(EnvironmentModel env, EntityFactory<T> entityFactory) {
        this.rowCount = env.getHeight();
        this.colCount = env.getWidth();
        this.cells = createCells(env.getWidth(), env.getHeight(), entityFactory);
        this.env = env;
        this.entityFactory = entityFactory;

        // Allocate cells.
        for (int row = 0; row < env.getHeight(); row++) {
            for (int col = 0; col < env.getWidth(); col++) {
                cells[row][col] = createCell(row, col, entityFactory);
            }
        }

        // Add entities to the appropriate cells.
        for (EntityModel entity : env.getEntities()) {
            setEntityCell(entity, getCell(entity));
        }
    }

    public EnvironmentModel getEnvironment() {
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

    public C getCell(int row, int col) {
        return cells[row][col];
    }

    public C getCell(EntityModel entity) {
        return cells[entity.getY()][entity.getX()];
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
        return isValidCellCoordinate(row, col) ? cells[row][col] : null;
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

    public void addEntity(EntityModel entity, C cell) {
        env.getEntities().add(entity);
        setEntityCell(entity, cell);
    }

    public void moveEntityToCell(T entityType, C fromCell, C targetCell) {
        EntityModel entity = fromCell.removeEntity(entityType);
        targetCell.setEntity(entity, entityType);
    }

    public void updateModel() {
        env.getEntities().clear();
        for (C[] row : cells) {
            for (C cell : row) {
                env.getEntities().addAll(cell.getEntities());
            }
        }
    }

    protected abstract C[][] createCells(int width, int height, EntityFactory<T> entityFactory);

    protected abstract C createCell(int row, int column, EntityFactory<T> entityFactory);

    private void setEntityCell(EntityModel entity, C cell) {
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
        for (C[] row : cells) {
            for (C cell : row) {
                for (T type : entityFactory.getTypeValues()) {
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
