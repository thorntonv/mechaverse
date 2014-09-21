package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;

/**
 * A module that can be added to an environment simulation.
 */
public interface EnvironmentSimulationModule extends EntityManager.Observer {

  /**
   * Performs any actions that are appropriate on before each update of the environment.
   */
  void beforeUpdate(AntSimulationState state, CellEnvironment env, EntityManager entityManager,
      RandomGenerator random);

  /**
   * Performs any actions that are appropriate before {@link ActiveEntity#performAction} is called.
   */
  void beforePerformAction(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random);

  /**
   * Performs any actions that are appropriate on after each update of the environment.
   */
  void afterUpdate(AntSimulationState state, CellEnvironment env, EntityManager entityManager,
      RandomGenerator random);
}
