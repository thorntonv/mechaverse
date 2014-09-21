package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt;
import org.mechaverse.simulation.ant.core.entity.ant.SimpleAntBehavior;

import com.google.common.base.Optional;

public class SimpleActiveEntityProvider implements ActiveEntityProvider {

  @Override
  public Optional<ActiveEntity> getActiveEntity(Entity entity) {
    if (entity instanceof Ant) {
      Ant ant = (Ant) entity;
      return Optional.<ActiveEntity>of(new ActiveAnt(ant, new SimpleAntBehavior()));
    }
    return Optional.absent();
  }
}
