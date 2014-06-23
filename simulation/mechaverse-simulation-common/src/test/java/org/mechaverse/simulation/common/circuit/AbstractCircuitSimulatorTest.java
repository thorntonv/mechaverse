package org.mechaverse.simulation.common.circuit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;

/**
 * Abstract base test for {@link CircuitSimulator} implementations.
 *
 * @author thorntonv@mechaverse.org
 */
public abstract class AbstractCircuitSimulatorTest {

  private static final Circuit CIRCUIT1 = CircuitTestUtil.createTestCircuit(1, 1,
    CircuitTestUtil.createRouting3in3OutElementType(), 1, 2);

  protected abstract CircuitSimulator newCircuitSimulator(Circuit circuit, int circuitCount)
      throws Exception;

  @Test
  public void simpleCircuit() throws Exception {
    CircuitSimulator circuitSimulator = newCircuitSimulator(CIRCUIT1, 1);

    // e1_out1 = (ex_in2 & e1_out1_input2Mask) | (e2_out1 & e1_out1_input3Mask);
    assertEquals(18, circuitSimulator.getCircuitStateSize());
    int[] state = new int[18];
    state[1] = 0b001; // ex_in2
    state[3] = 0b111; // e1_out1_input2Mask
    state[9] = 0b101; // e2_out1
    state[4] = 0b110; // e1_out1_input3Mask
    circuitSimulator.setCircuitState(0, state);
    circuitSimulator.update();
    circuitSimulator.getCircuitState(0, state);
    assertEquals(0b101, state[0]); // e1_out1
  }
}
