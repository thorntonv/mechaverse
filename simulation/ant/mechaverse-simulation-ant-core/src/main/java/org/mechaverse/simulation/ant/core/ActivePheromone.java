package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Pheromone;

/**
 * A pheromone that decays over time.
 */
public class ActivePheromone implements ActiveEntity {

  private final Pheromone entity;

  public ActivePheromone(Pheromone entity) {
    this.entity = entity;
  }

  @Override
  public void updateInput(CellEnvironment env, RandomGenerator random) {}

  @Override
  public void performAction(
      CellEnvironment env, EntityManager entityManager, RandomGenerator random) {
    entity.setEnergy(entity.getEnergy() - 1);
    if (entity.getEnergy() <= 0) {
      entityManager.removeEntity(this);
    }
  }

  @Override
  public Entity getEntity() {
    return entity;
  }

  @Override
  public EntityType getType() {
    return EntityType.PHEROMONE;
  }

  @Override
  public void updateModel() {}
}
