package org.mechaverse.simulation.common.circuit;

import org.mechaverse.circuit.model.Circuit;

/**
 * Provides a {@link Circuit}.
 */
public interface CircuitDataSource {

  /**
   * Returns the circuit provided by this data source.
   */
  public Circuit getCircuit();
}
