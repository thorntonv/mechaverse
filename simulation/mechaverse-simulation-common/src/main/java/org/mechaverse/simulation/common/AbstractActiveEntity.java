package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCell;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellEnvironment;
import org.mechaverse.simulation.common.model.Entity;

/**
 * An entity that is active in the environment.
 */
public interface AbstractActiveEntity<M, S extends SimulationState<M>, T extends Enum<T>, C extends AbstractCell<T>,
        E extends AbstractCellEnvironment<T, C>> {

  void updateInput(E env, RandomGenerator random);
  void performAction(E env, EntityManager entityManager, RandomGenerator random);

  Entity getEntity();
  T getType();

  void setState(S state);
  void updateState(S state);
  void onRemoveEntity();
}
