package org.mechaverse.simulation.ant.core;

/**
 * A pool of {@link AntSimulation} instances.
 *
 * @author Vance Thornton
 */
public interface AntSimulationPool {

  /**
   * Returns the total number of simulations in the pool.
   */
  int size();

  /**
   * Returns the instance with the given index from the pool.
   */
  AntSimulation getInstance(int idx);
}
