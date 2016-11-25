package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCell;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellEnvironment;
import org.mechaverse.simulation.common.model.Entity;

/**
 * An interface for managing the addition and removal of entities from an environment.
 */
public interface EntityManager<M, S extends SimulationState<M>, T extends Enum<T>, C extends AbstractCell<T>,
    E extends AbstractCellEnvironment<T, C>, A extends AbstractActiveEntity<M, S, T, C, E>> {

  interface Observer<M, S extends SimulationState<M>> {

    void onAddEntity(Entity entity, S state);
    void onRemoveEntity(Entity entity, S state);
  }

  void addEntity(Entity entity);
  void removeEntity(Entity entity);
  void removeEntity(A activeEntity);

  void addObserver(EntityManager.Observer<M, S> observer);
  void removeObserver(EntityManager.Observer<M, S> observer);
}
