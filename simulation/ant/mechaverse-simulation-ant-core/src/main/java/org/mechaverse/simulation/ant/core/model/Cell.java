package org.mechaverse.simulation.ant.core.model;

import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellModel;
import org.mechaverse.simulation.common.model.EntityModel;

public class Cell extends AbstractCellModel<EntityModel<EntityType>, EntityType> {

    public Cell(int row, int column) {
        super(row, column);
    }
}
