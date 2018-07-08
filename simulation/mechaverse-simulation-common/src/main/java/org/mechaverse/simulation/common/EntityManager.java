package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.model.EntityModel;

/**
 * An interface for managing the addition and removal of entities from an environment.
 */
@SuppressWarnings("unused")
public interface EntityManager<M, S extends SimulationState<M>> {

  interface Observer<M, S extends SimulationState<M>> {

    void onAddEntity(EntityModel entity, S state);
    void onRemoveEntity(EntityModel entity, S state);
  }

  void addEntity(EntityModel entity);
  void removeEntity(EntityModel entity);

  void addObserver(EntityManager.Observer<M, S> observer);
  void removeObserver(EntityManager.Observer<M, S> observer);
}
