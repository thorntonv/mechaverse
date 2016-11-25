package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCell;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellEnvironment;
import org.mechaverse.simulation.common.model.Entity;

/**
 * Provides an {@link AbstractActiveEntity} instance for the given entity.
 */
public interface AbstractActiveEntityProvider<M, S extends SimulationState<M>, T extends Enum<T>, C extends AbstractCell<T>,
    E extends AbstractCellEnvironment<T, C>, A extends AbstractActiveEntity<M, S, T, C, E>> {

  A getActiveEntity(Entity entity);
}
