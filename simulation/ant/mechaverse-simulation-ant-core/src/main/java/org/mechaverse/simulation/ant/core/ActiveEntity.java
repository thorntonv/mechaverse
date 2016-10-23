package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.ant.core.model.EntityType;

/**
 * An entity that is active in the environment.
 */
public interface ActiveEntity {

  void updateInput(CellEnvironment env, RandomGenerator random);
  void performAction(CellEnvironment env, EntityManager entityManager, RandomGenerator random);

  Entity getEntity();
  EntityType getType();

  void setState(AntSimulationState state);
  void updateState(AntSimulationState state);
  void onRemoveEntity();
}
