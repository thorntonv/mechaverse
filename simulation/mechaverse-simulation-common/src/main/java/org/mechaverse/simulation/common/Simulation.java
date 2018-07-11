package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * Interface for a mechaverse simulation.
 */
public interface Simulation<SIM_MODEL extends SimulationModel> {

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

  /**
   * Performs one or more iterations until the target fitness is reached.
   */
  void step(int stepCount, double targetFitness);

}
