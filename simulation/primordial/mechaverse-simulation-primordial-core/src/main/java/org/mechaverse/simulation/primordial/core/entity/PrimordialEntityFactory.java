package org.mechaverse.simulation.primordial.core.entity;

import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.model.Entity;

/**
 * PrimordialEntity entity factory.
 */
public class PrimordialEntityFactory implements EntityFactory<EntityType> {

    @Override
    public Entity newEntity(final EntityType entityType) {
        return EntityUtil.newEntity(entityType);
    }

    @Override
    public EntityType getType(final Entity entity) {
        return EntityUtil.getType(entity);
    }

    @Override
    public EntityType[] getTypeValues() {
        return EntityUtil.ENTITY_TYPES;
    }
}