package org.mechaverse.simulation.ant.core;

import java.util.Map;
import java.util.Map.Entry;

import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.entity.EntityUtil;

public final class ActiveEntityProviders {

  private final ActiveEntityProvider[] activeEntityProviders =
      new ActiveEntityProvider[EntityUtil.ENTITY_TYPES.length];

  public ActiveEntityProviders(Map<EntityType, ActiveEntityProvider> activeEntityProviderMap) {
    for (Entry<EntityType, ActiveEntityProvider> entry : activeEntityProviderMap.entrySet()) {
      activeEntityProviders[entry.getKey().ordinal()] = entry.getValue();
    }
  }

  public ActiveEntityProvider get(Entity entity) {
    return get(EntityUtil.getType(entity));
  }

  public ActiveEntityProvider get(EntityType entityType) {
    return activeEntityProviders[entityType.ordinal()];
  }
}
