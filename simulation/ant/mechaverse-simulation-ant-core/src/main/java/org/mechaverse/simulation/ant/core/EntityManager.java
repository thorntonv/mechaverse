package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.model.Entity;

/**
 * An interface for managing the addition and removal of entities from an environment.
 */
public interface EntityManager {

  void addEntity(Entity newEntity);
  void removeEntity(Entity entity);
  void removeEntity(ActiveEntity activeEntity);
}