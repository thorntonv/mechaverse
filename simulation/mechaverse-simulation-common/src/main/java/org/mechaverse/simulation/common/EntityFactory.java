package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.model.EntityModel;

/**
 * Interface for entity creation.
 */
public interface EntityFactory<T extends Enum<T>> {

    EntityModel newEntity(T entityType);

    T getType(EntityModel entity);

    T[] getTypeValues();
}
