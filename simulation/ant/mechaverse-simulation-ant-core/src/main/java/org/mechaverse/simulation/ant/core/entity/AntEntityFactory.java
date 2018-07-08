package org.mechaverse.simulation.ant.core.entity;

import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.model.EntityModel;

/**
 * Ant entity factory.
 */
public class AntEntityFactory implements EntityFactory<EntityType> {

    @Override
    public EntityModel newEntity(final EntityType entityType) {
        return EntityUtil.newEntity(entityType);
    }

    @Override
    public EntityType getType(final EntityModel entity) {
        return EntityUtil.getType(entity);
    }

    @Override
    public EntityType[] getTypeValues() {
        return EntityUtil.ENTITY_TYPES;
    }
}
