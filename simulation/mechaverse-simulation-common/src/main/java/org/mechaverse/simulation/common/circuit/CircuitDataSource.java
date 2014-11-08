package org.mechaverse.simulation.common.circuit;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;

/**
 * Provides a {@link Circuit}.
 */
public interface CircuitDataSource {

  /**
   * Returns the circuit provided by this data source.
   */
  public Circuit getCircuit();

  /**
   * Returns the {@link CircuitSimulationModel} for the circuit provided by this data source.
   */
  public CircuitSimulationModel getCircuitSimulationModel();
}
