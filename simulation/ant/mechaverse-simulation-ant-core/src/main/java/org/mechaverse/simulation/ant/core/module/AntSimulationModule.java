package org.mechaverse.simulation.ant.core.module;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.AntSimulationState;
import org.mechaverse.simulation.ant.core.ActiveEntity;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;

/**
 * A module that can be added to an ant simulation.
 */
public interface AntSimulationModule extends EntityManager.Observer {

  /**
   * Sets the state of the module before iterations are performed.
   */
  void setState(AntSimulationState state, CellEnvironment env, EntityManager entityManager);

  /**
   * Updates the {@link AntSimulationState}.
   */
  void updateState(AntSimulationState state, CellEnvironment env, EntityManager entityManager);

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
