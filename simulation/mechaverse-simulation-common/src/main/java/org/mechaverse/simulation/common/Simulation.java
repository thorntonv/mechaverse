package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.datastore.SimulationDataStore;

/**
 * Interface for a mechaverse simulation.
 */
public interface Simulation {

  /**
   * Returns the state of the simulation.
   */
  SimulationDataStore getState() throws Exception;

  /**
   * Sets the state of the simulation.
   */
  void setState(SimulationDataStore stateDataStore) throws Exception;

  /**
   * Generates a random state.
   */
  SimulationDataStore generateRandomState() throws Exception;

  /**
   * Performs one or more iterations.
   */
  void step(int stepCount) throws Exception;

  /**
   * Performs one or more iterations until the target fitness is reached.
   */
  void step(int stepCount, double targetFitness) throws Exception;

  /**
   * Returns a string with information about the device used by the simulation.
   */
  String getDeviceInfo();
}
