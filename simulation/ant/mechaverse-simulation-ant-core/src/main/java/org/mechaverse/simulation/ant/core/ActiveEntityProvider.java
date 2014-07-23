package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.model.Entity;

import com.google.common.base.Optional;

/**
 * Provides an {@link ActiveEntity} instance for the given entity (if appropriate).
 */
public interface ActiveEntityProvider {

  Optional<ActiveEntity> getActiveEntity(Entity entity);
}
