package org.mechaverse.simulation.ant.core.entity;

import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.model.Entity;

/**
 * Ant entity factory.
 */
public class AntEntityFactory implements EntityFactory<EntityType> {

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
