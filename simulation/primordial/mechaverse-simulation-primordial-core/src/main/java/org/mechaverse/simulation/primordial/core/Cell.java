package org.mechaverse.simulation.primordial.core;

import org.mechaverse.simulation.primordial.core.entity.PrimordialEntityFactory;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCell;

public class Cell extends AbstractCell<EntityType> {

    public Cell(int row, int column) {
        super(row, column, new PrimordialEntityFactory());
    }
}
