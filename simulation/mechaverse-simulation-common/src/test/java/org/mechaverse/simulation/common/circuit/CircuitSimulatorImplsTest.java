package org.mechaverse.simulation.common.circuit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitBuilder.Routing3In3OutElementType;
import org.mechaverse.simulation.common.circuit.generator.java.JavaCircuitSimulator;
import org.mechaverse.simulation.common.opencl.OpenClCircuitSimulator;
import org.mechaverse.simulation.common.util.compiler.JavaCompilerUtil.CompileException;

import com.jogamp.opencl.CLPlatform;

/**
 * A test that verifies that all circuit simulation implementations yield consistent results.
 *
 * @author thorntonv@mechaverse.org
 */
public class CircuitSimulatorImplsTest {

  @Test
  public void randomRoutingCircuits() throws CompileException {
    int numCircuits = 10;
    Circuit routingCircuit = CircuitBuilder.newCircuit(
        8, 8, Routing3In3OutElementType.newInstance(), 4, 4);
    routingCircuit.setIterationsPerUpdate(200);

    List<CircuitSimulator> simulators = getSimulators(numCircuits, routingCircuit);
    assertTrue(simulators.size() > 0);

    CircuitSimulator firstSimulator = simulators.get(0);
    int[] state = new int[firstSimulator.getCircuitStateSize()];
    CircuitTestUtil.setRandomState(state);

    for (CircuitSimulator simulator : simulators) {
      assertEquals(state.length, simulator.getCircuitStateSize());
      for (int circuitIndex = 0; circuitIndex < numCircuits; circuitIndex++) {
        simulator.setCircuitState(circuitIndex, state);
      }
    }
    for (CircuitSimulator simulator : simulators) {
      simulator.update();
    }

    int[] expectedState = new int[state.length];
    for (int circuitIndex = 0; circuitIndex < numCircuits; circuitIndex++) {
      firstSimulator.getCircuitState(circuitIndex, expectedState);
      for(CircuitSimulator simulator : simulators) {
        simulator.getCircuitState(circuitIndex, state);
        assertArrayEquals(expectedState, state);
      }
    }
  }

  public List<CircuitSimulator> getSimulators(int numCircuits, Circuit circuit)
      throws CompileException {
    List<CircuitSimulator> simulators = new ArrayList<>();
    simulators.add(new JavaCircuitSimulator(numCircuits, circuit));
    simulators.add(new OpenClCircuitSimulator(
      numCircuits, 16, 16, CLPlatform.getDefault().getMaxFlopsDevice(), circuit));
    return simulators;
  }
}
