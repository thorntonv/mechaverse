package org.mechaverse.simulation.ant.core;

/**
 * An ant simulation pool that holds a fixed number of simulations.
 *
 * @author Vance Thornton
 */
public class FixedAntSimulationPool implements AntSimulationPool {

  private final AntSimulation[] simulations;

  public FixedAntSimulationPool(int count) {
    this.simulations = new AntSimulation[count];
  }

  @Override
  public int size() {
    return simulations.length;
  }

  @Override
  public AntSimulation getInstance(int idx) {
    if (simulations[idx] == null) {
      simulations[idx] = new AntSimulation();
    }
    return simulations[idx];
  }
}
