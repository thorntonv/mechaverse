package org.mechaverse.simulation.primordial.core.module;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.primordial.core.CellEnvironment;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationState;

/**
 * A module that can be added to an primordial simulation.
 */
public interface PrimordialSimulationModule extends EntityManager.Observer<SimulationModel, PrimordialSimulationState> {

  /**
   * Sets the state of the module before iterations are performed.
   */
  void setState(PrimordialSimulationState state, CellEnvironment env, EntityManager entityManager);

  /**
   * Updates the {@link PrimordialSimulationState}.
   */
  void updateState(PrimordialSimulationState state, CellEnvironment env, EntityManager entityManager);

  /**
   * Performs any actions that are appropriate on before each update of the environment.
   */
  void beforeUpdate(PrimordialSimulationState state, CellEnvironment env, EntityManager entityManager,
      RandomGenerator random);

  /**
   * Performs any actions that are appropriate before {@link ActiveEntity#performAction} is called.
   */
  void beforePerformAction(PrimordialSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random);

  /**
   * Performs any actions that are appropriate on after each update of the environment.
   */
  void afterUpdate(PrimordialSimulationState state, CellEnvironment env, EntityManager entityManager,
      RandomGenerator random);
}
