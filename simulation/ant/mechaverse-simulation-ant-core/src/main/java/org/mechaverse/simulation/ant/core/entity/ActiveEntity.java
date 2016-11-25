package org.mechaverse.simulation.ant.core.entity;

import org.mechaverse.simulation.ant.core.AntSimulationState;
import org.mechaverse.simulation.ant.core.Cell;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.AbstractActiveEntity;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * Binds {@link AbstractActiveEntity} generic parameters for the ant simulation.
 */
public interface ActiveEntity extends AbstractActiveEntity<SimulationModel, AntSimulationState, EntityType, Cell, CellEnvironment> {
}
