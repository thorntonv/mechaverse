package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * A behavior that can be added to an environment.
 */
public interface EnvironmentBehavior<
        SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
        ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
        ENT_MODEL extends EntityModel<ENT_TYPE>,
        ENT_TYPE extends Enum<ENT_TYPE>> extends SimulationObserver<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> {

  /**
   * Sets the state of the behavior before iterations are performed.
   */
  void setState(SIM_MODEL state, Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environment);

  /**
   * Updates the {@link SimulationModel}.
   */
  void updateState(SIM_MODEL state, Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environment);

  /**
   * Performs any actions that are appropriate before each update of the environment.
   */
  void beforeUpdate(SIM_MODEL state, Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environment,
          RandomGenerator random);

  /**
   * Performs any actions that are appropriate before {@link EntityBehavior#performAction} is called.
   */
  void beforePerformAction(SIM_MODEL state, Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environment,
          RandomGenerator random);

  /**
   * Performs any actions that are appropriate after each update of the environment.
   */
  void afterUpdate(SIM_MODEL state, Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environment,
          RandomGenerator random);
}