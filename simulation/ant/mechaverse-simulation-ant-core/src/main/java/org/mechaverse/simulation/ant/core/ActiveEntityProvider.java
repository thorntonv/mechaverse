package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.common.model.Entity;

/**
 * Provides an {@link ActiveEntity} instance for the given entity.
 */
public interface ActiveEntityProvider {

  ActiveEntity getActiveEntity(Entity entity);
}
