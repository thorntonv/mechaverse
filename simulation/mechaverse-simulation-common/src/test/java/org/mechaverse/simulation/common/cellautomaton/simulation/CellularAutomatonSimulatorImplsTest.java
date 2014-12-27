package org.mechaverse.simulation.common.cellautomaton.simulation;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.Routing3In3OutCellType;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.java.JavaCellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.java.JavaCellularAutomatonSimulatorTest;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;
import org.mechaverse.simulation.common.util.compiler.JavaCompilerUtil.CompileException;

import com.jogamp.opencl.CLPlatform;

/**
 * A test that verifies that all cellular automaton simulation implementations yield consistent
 * results.
 */
public class CellularAutomatonSimulatorImplsTest {

  @Test
  public void randomRoutingAutomata() throws CompileException {
    int numAutomata = 10;
    CellularAutomatonDescriptor routingDescriptor = CellularAutomatonBuilder.newCellularAutomaton(
        8, 8, Routing3In3OutCellType.newInstance(), 4, 4);
    routingDescriptor.setIterationsPerUpdate(200);

    List<CellularAutomatonSimulator> simulators = getSimulators(numAutomata, routingDescriptor);
    assertTrue(simulators.size() > 0);

    CellularAutomatonSimulator firstSimulator = simulators.get(0);
    int[] state = new int[firstSimulator.getAutomatonStateSize()];
    CellularAutomatonTestUtil.setRandomState(state);

    for (CellularAutomatonSimulator simulator : simulators) {
      assertEquals(state.length, simulator.getAutomatonStateSize());
      for (int idx = 0; idx < numAutomata; idx++) {
        simulator.setAutomatonState(idx, state);
      }
    }
    for (CellularAutomatonSimulator simulator : simulators) {
      simulator.update();
    }

    int[] expectedState = new int[state.length];
    for (int index = 0; index < numAutomata; index++) {
      firstSimulator.getAutomatonState(index, expectedState);
      for(CellularAutomatonSimulator simulator : simulators) {
        simulator.getAutomatonState(index, state);
        assertArrayEquals(expectedState, state);
      }
    }
  }

  public List<CellularAutomatonSimulator> getSimulators(
      int numAutomata, CellularAutomatonDescriptor descriptor) throws CompileException {
    List<CellularAutomatonSimulator> simulators = new ArrayList<>();
    simulators.add(new JavaCellularAutomatonSimulator(numAutomata,
        JavaCellularAutomatonSimulatorTest.INPUT_SIZE, 
            JavaCellularAutomatonSimulatorTest.OUTPUT_SIZE, descriptor));
    simulators.add(new OpenClCellularAutomatonSimulator(
        numAutomata, 16, 16, CLPlatform.getDefault().getMaxFlopsDevice(), descriptor));
    return simulators;
  }
}
