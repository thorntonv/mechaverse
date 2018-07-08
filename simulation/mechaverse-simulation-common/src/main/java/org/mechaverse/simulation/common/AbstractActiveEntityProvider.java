package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCell;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellEnvironment;
import org.mechaverse.simulation.common.model.EntityModel;

/**
 * Provides an {@link ActiveEntity} instance for the given entity.
 */
public interface AbstractActiveEntityProvider<M, S extends SimulationState<M>, T extends Enum<T>, C extends AbstractCell<T>,
    E extends AbstractCellEnvironment<T, C>, A extends ActiveEntity<M, S, T, C, E>> {

  A getActiveEntity(EntityModel entity);
}
