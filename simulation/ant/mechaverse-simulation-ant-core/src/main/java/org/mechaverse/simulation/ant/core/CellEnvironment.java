package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.core.entity.AntEntityFactory;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.model.Nest;
import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellEnvironment;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;

public class CellEnvironment extends AbstractCellEnvironment<EntityType, Cell> {

    private Direction[][] nestDirectionIndex;

    public CellEnvironment(EnvironmentModel env) {
        super(env, new AntEntityFactory());

        for (EntityModel entity : env.getEntities()) {
            if (entity instanceof Nest) {
                Cell nestCell = getCell(entity);
                nestDirectionIndex = new Direction[getRowCount()][getColumnCount()];
                for (int row = 0; row < getRowCount(); row++) {
                    for (int col = 0; col < getColumnCount(); col++) {
                        nestDirectionIndex[row][col] = getDirection(cells[row][col], nestCell);
                    }
                }
            }
        }
    }

    @Override
    protected Cell[][] createCells(final int width, final int height, final EntityFactory entityFactory) {
        return new Cell[height][width];
    }

    @Override
    protected Cell createCell(final int row, final int column, final EntityFactory<EntityType> entityFactory) {
        return new Cell(row, column);
    }

    public Direction getNestDirection(Cell fromCell) {
        return nestDirectionIndex[fromCell.getRow()][fromCell.getColumn()];
    }
}
