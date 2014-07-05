package org.mechaverse.simulation.common.circuit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitBuilder.CircuitStateBuilder;
import org.mechaverse.simulation.common.circuit.CircuitBuilder.LogicalUnitStateBuilder;
import org.mechaverse.simulation.common.circuit.CircuitBuilder.Routing3In3OutElementType;
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
  private Circuit routingCircuit;

  protected abstract CircuitSimulator newCircuitSimulator(Circuit circuit, int circuitCount)
      throws Exception;

  @Before
  public void setUp() {
    routingCircuit = CircuitBuilder.newCircuit(3, 3, Routing3In3OutElementType.newInstance(), 4, 4);
  }

  @Test
  public void routingCircuit_stateSize() throws Exception {
    CircuitSimulator circuitSimulator = newCircuitSimulator(routingCircuit, 1);

    int wordsPerElement = 3 + 2*3; // 3 outputs + 2 params per output.
    int elementsPerLu = 4 * 4;
    int luPerCircuit = 3 * 3;
    assertEquals(wordsPerElement * elementsPerLu * luPerCircuit,
        circuitSimulator.getCircuitStateSize());
  }

  @Test
  public void multipleCircuit_stateSize() throws Exception {
    CircuitSimulator circuitSimulator = newCircuitSimulator(routingCircuit, 5);

    int wordsPerElement = 3 + 2*3; // 3 outputs + 2 params per output.
    int elementsPerLu = 4 * 4;
    int luPerCircuit = 3 * 3;
    assertEquals(wordsPerElement * elementsPerLu * luPerCircuit,
        circuitSimulator.getCircuitStateSize());
  }

  @Test
  public void update_internalRoutingElement() throws Exception {
    CircuitStateBuilder stateBuilder = CircuitStateBuilder.of(routingCircuit, 1);
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

    CircuitSimulator circuitSimulator = newCircuitSimulator(routingCircuit, 1);
    circuitSimulator.setCircuitState(0, stateBuilder.getState());
    circuitSimulator.update();
    circuitSimulator.getCircuitState(0, stateBuilder.getState());

    assertEquals(0b110, luStateBuilder.get("e10_out1"));
    assertEquals(0b100, luStateBuilder.get("e10_out2"));
    assertEquals(0b010, luStateBuilder.get("e10_out3"));
  }

  @Test
  public void update_routingCircuit_luUpperRightBoundryElement() throws Exception {
    CircuitStateBuilder stateBuilder = CircuitStateBuilder.of(routingCircuit, 1);
    stateBuilder.setAll(0b111);

    // e4_out2 = e3_out3 or e1_out1 (lu to the right).
    stateBuilder.luStateBuilder(0, 5).set("e1_out1", 0b101);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 4);
    luStateBuilder.set("e3_out3", 0b010);
    luStateBuilder.set("e4_out2_input1Mask", 0b000);
    luStateBuilder.set("e4_out2_input3Mask", 0b110);

    CircuitSimulator circuitSimulator = newCircuitSimulator(routingCircuit, 1);
    circuitSimulator.setCircuitState(0, stateBuilder.getState());
    circuitSimulator.update();
    circuitSimulator.getCircuitState(0, stateBuilder.getState());

    assertEquals(0b100, luStateBuilder.get("e4_out2"));
  }

  @Test
  public void update_routingCircuit_luLowerRightBoundryElement() throws Exception {
    CircuitStateBuilder stateBuilder = CircuitStateBuilder.of(routingCircuit, 1);
    stateBuilder.setAll(0b111);

    // e16_out1 = e12_out2 or e13_out1 (lu to the right).
    stateBuilder.luStateBuilder(0, 5).set("e13_out1", 0b101);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 4);
    luStateBuilder.set("e12_out2", 0b010);
    luStateBuilder.set("e16_out1_input2Mask", 0b001);
    luStateBuilder.set("e16_out1_input3Mask", 0b110);

    CircuitSimulator circuitSimulator = newCircuitSimulator(routingCircuit, 1);
    circuitSimulator.setCircuitState(0, stateBuilder.getState());
    circuitSimulator.update();
    circuitSimulator.getCircuitState(0, stateBuilder.getState());

    assertEquals(0b101, luStateBuilder.get("e16_out1"));
  }

  @Test
  public void update_routingCircuit_circuitUpperLeftBoundryElement() throws Exception {
    CircuitStateBuilder stateBuilder = CircuitStateBuilder.of(routingCircuit, 1);
    stateBuilder.setAll(0b111);

    // e1_out2 = e4_out3 (lu to the left) or e2_out1.
    stateBuilder.luStateBuilder(0, 8).set("e4_out3", 0b101);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 0);
    luStateBuilder.set("e2_out1", 0b010);
    luStateBuilder.set("e1_out2_input1Mask", 0b111);
    luStateBuilder.set("e1_out2_input3Mask", 0b000);

    CircuitSimulator circuitSimulator = newCircuitSimulator(routingCircuit, 1);
    circuitSimulator.setCircuitState(0, stateBuilder.getState());
    circuitSimulator.update();
    circuitSimulator.getCircuitState(0, stateBuilder.getState());

    assertEquals(0b101, luStateBuilder.get("e1_out2"));
  }

  @Test
  public void update_routingCircuit_circuitLeftBoundryElement() throws Exception {
    CircuitStateBuilder stateBuilder = CircuitStateBuilder.of(routingCircuit, 1);
    stateBuilder.setAll(0b111);

    // e1_out2 = e4_out3 (lu to the left) or e2_out1.
    stateBuilder.luStateBuilder(0, 2).set("e4_out3", 0b101);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 3);
    luStateBuilder.set("e2_out1", 0b010);
    luStateBuilder.set("e1_out2_input1Mask", 0b111);
    luStateBuilder.set("e1_out2_input3Mask", 0b000);

    CircuitSimulator circuitSimulator = newCircuitSimulator(routingCircuit, 1);
    circuitSimulator.setCircuitState(0, stateBuilder.getState());
    circuitSimulator.update();
    circuitSimulator.getCircuitState(0, stateBuilder.getState());

    assertEquals(0b101, luStateBuilder.get("e1_out2"));
  }

  @Test
  public void update_multipleCircuits() throws Exception {
    int numCircuits = 50;
    CircuitSimulator circuitSimulator = newCircuitSimulator(routingCircuit, numCircuits);

    int[] state = new int[circuitSimulator.getCircuitStateSize()];
    CircuitTestUtil.setRandomState(state);
    for(int circuitIndex = 0; circuitIndex < numCircuits; circuitIndex++) {
      circuitSimulator.setCircuitState(circuitIndex, state);
    }
    circuitSimulator.update();
    int[] expectedState = new int[circuitSimulator.getCircuitStateSize()];
    circuitSimulator.getCircuitState(0, expectedState);
    for(int circuitIndex = 1; circuitIndex < numCircuits; circuitIndex++) {
      circuitSimulator.getCircuitState(circuitIndex, state);
      assertArrayEquals(expectedState, state);
    }
  }

  /**
   * Tests that state after a single update with N iterations is equivalent to N updates with a
   * single iteration.
   */
  @Test
  public void update_multipleIterations() throws Exception {
    int iterationsPerUpdate = 200;
    Circuit circuit1 = CircuitBuilder.newCircuit(
      3, 3, Routing3In3OutElementType.newInstance(), 4, 4);
    Circuit circuit2 = CircuitBuilder.newCircuit(
      3, 3, Routing3In3OutElementType.newInstance(), 4, 4);
    circuit2.setIterationsPerUpdate(iterationsPerUpdate);

    CircuitSimulator circuitSimulator1 = newCircuitSimulator(circuit1, 1);
    CircuitSimulator circuitSimulator2 = newCircuitSimulator(circuit2, 1);

    int[] state1 = new int[circuitSimulator1.getCircuitStateSize()];
    int[] state2 = new int[circuitSimulator1.getCircuitStateSize()];
    CircuitTestUtil.setRandomState(state1);
    circuitSimulator1.setCircuitState(0, state1);
    circuitSimulator2.setCircuitState(0, state1);

    for(int cnt = 0; cnt < iterationsPerUpdate; cnt++) {
      circuitSimulator1.update();
    }

    circuitSimulator2.update();

    circuitSimulator1.getCircuitState(0, state1);
    circuitSimulator2.getCircuitState(0, state2);

    assertArrayEquals(state1, state2);
  }
}
