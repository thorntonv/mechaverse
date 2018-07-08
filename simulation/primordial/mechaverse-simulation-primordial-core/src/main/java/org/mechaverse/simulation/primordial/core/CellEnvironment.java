package org.mechaverse.simulation.primordial.core;

import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellEnvironment;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.primordial.core.entity.PrimordialEntityFactory;
import org.mechaverse.simulation.primordial.core.model.EntityType;

public class CellEnvironment extends AbstractCellEnvironment<EntityType, Cell> {

    public CellEnvironment(EnvironmentModel env) {
        super(env, new PrimordialEntityFactory());
    }

    @Override
    protected Cell[][] createCells(final int width, final int height, final EntityFactory entityFactory) {
        return new Cell[height][width];
    }

    @Override
    protected Cell createCell(final int row, final int column, final EntityFactory<EntityType> entityFactory) {
        return new Cell(row, column);
    }
}
