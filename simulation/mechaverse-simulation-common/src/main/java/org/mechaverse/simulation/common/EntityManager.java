package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.model.Entity;

/**
 * An interface for managing the addition and removal of entities from an environment.
 */
@SuppressWarnings("unused")
public interface EntityManager<M, S extends SimulationState<M>> {

  interface Observer<M, S extends SimulationState<M>> {

    void onAddEntity(Entity entity, S state);
    void onRemoveEntity(Entity entity, S state);
  }

  void addEntity(Entity entity);
  void removeEntity(Entity entity);

  void addObserver(EntityManager.Observer<M, S> observer);
  void removeObserver(EntityManager.Observer<M, S> observer);
}
