package org.mechaverse.simulation.common.circuit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitTestUtil.CircuitStateBuilder;
import org.mechaverse.simulation.common.circuit.CircuitTestUtil.LogicalUnitStateBuilder;

/**
 * Abstract base test for {@link CircuitSimulator} implementations.
 *
 * @author thorntonv@mechaverse.org
 */
public abstract class AbstractCircuitSimulatorTest {

  // A simple routing circuit with 3x3 logical units. Each logical unit has 4x4 3-in/out routing
  // elements.
  //
  //        [2]       [3]
  // [1] ----1----2----3----4--- [4]
  //              |         |
  // [5] ----5----6----7----8--- [6]
  //         |         |
  // [7] ----9---10---11---12--- [8]
  //              |         |
  // [9] ---13---14---15---16--- [12]
  //       [10]      [11]
  //
  private static final Circuit ROUTING_CIRCUIT = CircuitTestUtil.createTestCircuit(3, 3,
    CircuitTestUtil.createRouting3in3OutElementType(), 4, 4);

  protected abstract CircuitSimulator newCircuitSimulator(Circuit circuit, int circuitCount)
      throws Exception;

  @Test
  public void routingCircuit_stateSize() throws Exception {
    CircuitSimulator circuitSimulator = newCircuitSimulator(ROUTING_CIRCUIT, 1);

    int wordsPerElement = 3 + 2*3; // 3 outputs + 2 params per output.
    int elementsPerLu = 4 * 4;
    int luPerCircuit = 3 * 3;
    assertEquals(wordsPerElement * elementsPerLu * luPerCircuit,
        circuitSimulator.getCircuitStateSize());
  }

  @Test
  public void update_internalRoutingElement() throws Exception {
    CircuitStateBuilder stateBuilder = CircuitStateBuilder.of(ROUTING_CIRCUIT, 1);
    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 0);

    // e10_out1 = e14_out2 or e11_out1.
    // e10_out2 = e9_out3 or e11_out1.
    // e10_out3 = e9_out3 or e14_out2.
    // e9 is updated before e10 and has value 0.
    luStateBuilder.set("e14_out2", 0b010);
    luStateBuilder.set("e11_out1", 0b100);
    luStateBuilder.set("e10_out1_input2Mask", 0b111);
    luStateBuilder.set("e10_out1_input3Mask", 0b110);
    luStateBuilder.set("e10_out2_input1Mask", 0b111);
    luStateBuilder.set("e10_out2_input3Mask", 0b111);
    luStateBuilder.set("e10_out3_input1Mask", 0b111);
    luStateBuilder.set("e10_out3_input2Mask", 0b111);

    CircuitSimulator circuitSimulator = newCircuitSimulator(ROUTING_CIRCUIT, 1);
    circuitSimulator.setCircuitState(0, stateBuilder.getState());
    circuitSimulator.update();
    circuitSimulator.getCircuitState(0, stateBuilder.getState());

    assertEquals(0b110, luStateBuilder.get("e10_out1"));
    assertEquals(0b100, luStateBuilder.get("e10_out2"));
    assertEquals(0b010, luStateBuilder.get("e10_out3"));
  }

  @Test
  public void update_routingCircuit_luUpperRightBoundryElement() throws Exception {
    CircuitStateBuilder stateBuilder = CircuitStateBuilder.of(ROUTING_CIRCUIT, 1);
    stateBuilder.setAll(0b111);

    // e4_out2 = e3_out3 or e1_out1 (lu to the right).
    stateBuilder.luStateBuilder(0, 5).set("e1_out1", 0b101);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 4);
    luStateBuilder.set("e3_out3", 0b010);
    luStateBuilder.set("e4_out2_input1Mask", 0b000);
    luStateBuilder.set("e4_out2_input3Mask", 0b110);

    CircuitSimulator circuitSimulator = newCircuitSimulator(ROUTING_CIRCUIT, 1);
    circuitSimulator.setCircuitState(0, stateBuilder.getState());
    circuitSimulator.update();
    circuitSimulator.getCircuitState(0, stateBuilder.getState());

    assertEquals(0b100, luStateBuilder.get("e4_out2"));
  }

  @Test
  public void update_routingCircuit_luLowerRightBoundryElement() throws Exception {
    CircuitStateBuilder stateBuilder = CircuitStateBuilder.of(ROUTING_CIRCUIT, 1);
    stateBuilder.setAll(0b111);

    // e16_out1 = e12_out2 or e13_out1 (lu to the right).
    stateBuilder.luStateBuilder(0, 5).set("e13_out1", 0b101);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 4);
    luStateBuilder.set("e12_out2", 0b010);
    luStateBuilder.set("e16_out1_input2Mask", 0b001);
    luStateBuilder.set("e16_out1_input3Mask", 0b110);

    CircuitSimulator circuitSimulator = newCircuitSimulator(ROUTING_CIRCUIT, 1);
    circuitSimulator.setCircuitState(0, stateBuilder.getState());
    circuitSimulator.update();
    circuitSimulator.getCircuitState(0, stateBuilder.getState());

    assertEquals(0b101, luStateBuilder.get("e16_out1"));
  }
}
