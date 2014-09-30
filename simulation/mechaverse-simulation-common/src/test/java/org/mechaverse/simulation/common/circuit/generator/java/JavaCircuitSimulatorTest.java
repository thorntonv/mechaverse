package org.mechaverse.simulation.common.circuit.generator.java;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.AbstractCircuitSimulatorTest;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;

/**
 * Unit test for {@link JavaCircuitSimulator}.
 */
public class JavaCircuitSimulatorTest extends AbstractCircuitSimulatorTest {

  public static final int CIRCUIT_INPUT_SIZE = 4;

  @Override
  protected CircuitSimulator newCircuitSimulator(Circuit circuit, int circuitCount)
      throws Exception {
    return new JavaCircuitSimulator(circuitCount, CIRCUIT_INPUT_SIZE, circuit);
  }
}
