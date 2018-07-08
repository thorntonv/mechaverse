package org.mechaverse.simulation.primordial.core.entity;

import org.mechaverse.simulation.primordial.core.PrimordialSimulationState;
import org.mechaverse.simulation.primordial.core.Cell;
import org.mechaverse.simulation.primordial.core.CellEnvironment;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * Binds {@link org.mechaverse.simulation.common.ActiveEntity} generic parameters for the primordial simulation.
 */
public interface ActiveEntity extends org.mechaverse.simulation.common.ActiveEntity<SimulationModel,
    PrimordialSimulationState, EntityType, Cell, CellEnvironment> {
}
