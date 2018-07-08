package org.mechaverse.simulation.primordial.core.entity;

import org.mechaverse.simulation.common.AbstractActiveEntityProvider;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.primordial.core.Cell;
import org.mechaverse.simulation.primordial.core.CellEnvironment;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationState;
import org.mechaverse.simulation.primordial.core.model.EntityType;

public interface ActiveEntityProvider extends AbstractActiveEntityProvider<SimulationModel,
    PrimordialSimulationState, EntityType, Cell, CellEnvironment, ActiveEntity> {
}
