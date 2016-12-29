package org.mechaverse.simulation.primordial.core.entity.primordial;

import org.mechaverse.simulation.primordial.core.entity.ActiveEntity;
import org.mechaverse.simulation.primordial.core.entity.ActiveEntityProvider;
import org.mechaverse.simulation.primordial.core.entity.primordial.ActivePrimordialEntity.PrimordialEntityBehavior;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntity;
import org.mechaverse.simulation.common.model.Entity;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides {@link ActivePrimordialEntity} instances.
 */
public class ActivePrimordialEntityProvider implements ActiveEntityProvider {

  @Autowired ObjectFactory<PrimordialEntityBehavior> behaviorFactory;

  @Override
  public ActiveEntity getActiveEntity(Entity entity) {
    return new ActivePrimordialEntity((PrimordialEntity) entity, behaviorFactory.getObject());
  }
}
