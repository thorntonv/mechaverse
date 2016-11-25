package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.model.Entity;

/**
 * Interface for entity creation.
 */
public interface EntityFactory<T extends Enum<T>> {

    Entity newEntity(T entityType);

    T getType(Entity entity);

    T[] getTypeValues();
}
