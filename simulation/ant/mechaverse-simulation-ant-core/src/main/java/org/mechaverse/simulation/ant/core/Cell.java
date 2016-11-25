package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.core.entity.AntEntityFactory;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCell;

public class Cell extends AbstractCell<EntityType> {

    public Cell(int row, int column) {
        super(row, column, new AntEntityFactory());
    }
}
