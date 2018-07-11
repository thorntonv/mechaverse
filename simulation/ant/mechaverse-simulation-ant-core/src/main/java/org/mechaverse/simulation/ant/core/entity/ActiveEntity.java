package org.mechaverse.simulation.ant.core.entity;

import org.mechaverse.simulation.ant.core.Cell;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * Binds {@link org.mechaverse.simulation.common.ActiveEntity} generic parameters for the ant simulation.
 */
public interface ActiveEntity extends
    org.mechaverse.simulation.common.ActiveEntity<SimulationModel, AntSimulationState, EntityType, Cell, CellEnvironment> {
}
