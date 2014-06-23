package org.mechaverse.simulation.common.circuit.generator.java;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.AbstractCircuitSimulatorTest;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;

/**
 * Unit test for {@link JavaCircuitSimulator}.
 *
 * @author thorntonv@mechaverse.org
 */
public class JavaCircuitSimulatorTest extends AbstractCircuitSimulatorTest {

  @Override
  protected CircuitSimulator newCircuitSimulator(Circuit circuit, int circuitCount)
      throws Exception {
    return new JavaCircuitSimulator(circuitCount, circuit);
  }
}
