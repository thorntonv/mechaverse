package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * A module that can be added to an environment simulation.
 */
public interface EnvironmentSimulationModule extends EntityManager.Observer {

  /**
   * Performs any actions that are appropriate on each update of the environment.
   */
  void update(CellEnvironment env, EntityManager entityManager, RandomGenerator random);
}
