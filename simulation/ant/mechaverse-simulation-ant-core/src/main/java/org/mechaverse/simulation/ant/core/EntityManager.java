package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.model.Entity;

/**
 * An interface for managing the addition and removal of entities from an environment.
 */
public interface EntityManager {

  public static interface Observer {

    void onAddEntity(Entity entity);
    void onRemoveEntity(Entity entity);
  }

  void addEntity(Entity entity);
  void removeEntity(Entity entity);
  void removeEntity(ActiveEntity activeEntity);

  void addObserver(EntityManager.Observer observer);
  void removeObserver(EntityManager.Observer observer);
}