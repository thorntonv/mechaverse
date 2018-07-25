package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * Interface for a mechaverse simulation.
 */
public interface Simulation<
        SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
        ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
        ENT_MODEL extends EntityModel<ENT_TYPE>,
        ENT_TYPE extends Enum<ENT_TYPE>> {

  /**
   * Returns the state of the simulation.
   */
  SIM_MODEL getState();

  /**
   * Sets the state of the simulation.
   */
  void setState(SIM_MODEL model) throws Exception;

  /**
   * Generates a random state.
   */
  SIM_MODEL generateRandomState();

  /**
   * Performs one or more iterations.
   */
  void step(int stepCount);

  void addObserver(SimulationObserver<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> observer);

  void removeObserver(SimulationObserver<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> observer);
}
