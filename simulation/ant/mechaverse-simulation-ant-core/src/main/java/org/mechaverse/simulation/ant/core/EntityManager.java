package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Entity;

/**
 * An interface for managing the addition and removal of entities from an environment.
 */
public interface EntityManager {

  interface Observer {

    void onAddEntity(Entity entity, AntSimulationState state);
    void onRemoveEntity(Entity entity, AntSimulationState state);
  }

  void addEntity(Entity entity);
  void removeEntity(Entity entity);
  void removeEntity(ActiveEntity activeEntity);

  void addObserver(EntityManager.Observer observer);
  void removeObserver(EntityManager.Observer observer);
}
