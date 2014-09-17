package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationConfig;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;

/**
 * An entity that is active in the environment.
 */
public interface ActiveEntity {

  void updateInput(CellEnvironment env, RandomGenerator random);
  void performAction(CellEnvironment env, AntSimulationConfig config, EntityManager entityManager,
      RandomGenerator random);

  Entity getEntity();
  EntityType getType();
  void updateModel();
}
