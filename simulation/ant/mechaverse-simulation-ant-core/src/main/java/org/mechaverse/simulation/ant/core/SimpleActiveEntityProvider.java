package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.Nest;
import org.mechaverse.simulation.ant.api.model.Pheromone;

import com.google.common.base.Optional;

public class SimpleActiveEntityProvider implements ActiveEntityProvider {

  @Override
  public Optional<ActiveEntity> getActiveEntity(Entity entity) {
    if (entity instanceof Ant) {
      Ant ant = (Ant) entity;
      return Optional.<ActiveEntity>of(new ActiveAnt(ant, new SimpleAntBehavior()));
    } else if (entity instanceof Pheromone) {
      return Optional.<ActiveEntity>of(new ActivePheromone((Pheromone) entity));
    } else if (entity instanceof Nest) {
      return Optional.<ActiveEntity>of(new ActiveNest((Nest) entity));
    }
    return Optional.absent();
  }
}
