package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * A behavior that can be added to an environment.
 */
public interface EnvironmentBehavior<
    SIM_MODEL extends SimulationModel,
    ENV_MODEL extends EnvironmentModel,
    ENT_MODEL extends EntityModel> extends EntityManager.Observer<SIM_MODEL, ENT_MODEL> {

  /**
   * Sets the state of the module before iterations are performed.
   */
  void setState(SIM_MODEL state, ENV_MODEL environmentModel,
      EntityManager<SIM_MODEL, ENT_MODEL> entityManager);

  /**
   * Updates the {@link SimulationModel}.
   */
  void updateState(SIM_MODEL state, ENV_MODEL environmentModel,
      EntityManager<SIM_MODEL, ENT_MODEL> entityManager);

  /**
   * Performs any actions that are appropriate on before each update of the environment.
   */
  void beforeUpdate(SIM_MODEL state, ENV_MODEL environmentModel,
      EntityManager<SIM_MODEL, ENT_MODEL> entityManager, RandomGenerator random);

  /**
   * Performs any actions that are appropriate before {@link EntityBehavior#performAction} is called.
   */
  void beforePerformAction(SIM_MODEL state, ENV_MODEL environmentModel,
      EntityManager<SIM_MODEL, ENT_MODEL> entityManager, RandomGenerator random);

  /**
   * Performs any actions that are appropriate on after each update of the environment.
   */
  void afterUpdate(SIM_MODEL state, ENV_MODEL environmentModel,
      EntityManager<SIM_MODEL, ENT_MODEL> entityManager, RandomGenerator random);
}