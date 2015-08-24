package org.mechaverse.simulation.common.cellautomaton.simulation.opencl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.AbstractCellularAutomatonSimulatorTest;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;

import com.jogamp.opencl.CLPlatform;

/**
 * Unit test for {@link OpenClCellularAutomatonSimulator}.
 */
public class OpenClCellularAutomatonSimulatorTest extends AbstractCellularAutomatonSimulatorTest {

  private static final String KERNAL_SOURCE =
      "void kernel " + OpenClCellularAutomatonSimulator.KERNEL_NAME + "(" +
      "    global const int* inputMap, global const int* input, global int* state, " +
      "    global int* outMap, global int* output, const unsigned int inputLength, " +
      "    const unsigned int outputLength) {" +
      "  output[get_global_id(0)] = state[get_global_id(0)] + input[get_global_id(0)];" +
      "  state[get_global_id(0)]++;" +
      "}";

  @Test
  public void update() throws Exception {
    int input[] = {10};
    int state[] = {5};
    int output[] = {0};

    try (OpenClCellularAutomatonSimulator simulator = new OpenClCellularAutomatonSimulator(
        1, 1, 1, 1, 1, 1, CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE)) {
      simulator.setAutomatonInput(0, input);
      simulator.setAutomatonState(0, state);
      simulator.update();
      simulator.getAutomatonOutput(0, output);
      simulator.getAutomatonState(0, state);

      assertEquals(15, output[0]);
      assertEquals(6, state[0]);
    }
  }

  @Test
  public void multipleUpdate() throws Exception {
    int input[] = {10};
    int state[] = {5};
    int output[] = {0};

    try (OpenClCellularAutomatonSimulator simulator = new OpenClCellularAutomatonSimulator(
      1, 1, 1, 1, 1, 1, CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE)) {
        simulator.setAutomatonInput(0, input);
        simulator.setAutomatonState(0, state);

      // Perform 10 updates.
      for (int cnt = 1; cnt <= 10; cnt++) {
        simulator.update();
      }

      simulator.getAutomatonOutput(0, output);
      simulator.getAutomatonState(0, state);

      assertEquals(5 + 9 + 10, output[0]);
      assertEquals(15, state[0]);
    }
  }

  @Test
  public void multipleAutomata() throws Exception {
    // Two automata each with 5 cells.
    int input1[] = {1, 2, 3, 4, 5};
    int state1[] = {1, 1, 1, 1, 1};
    int input2[] = {6, 7, 8, 9, 0};
    int state2[] = {5, 5, 5, 5, 5};
    int output[] = {0, 0, 0, 0, 0};

    try (OpenClCellularAutomatonSimulator simulator = new OpenClCellularAutomatonSimulator(
      2, 5, 5, 5, 10, 5, CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE)) {

      assertEquals(5, simulator.getAutomatonInputSize());
      assertEquals(5, simulator.getAutomatonStateSize());
      assertEquals(5, simulator.getAutomatonOutputSize());
      simulator.setAutomatonInput(0, input1);
      simulator.setAutomatonState(0, state1);
      simulator.setAutomatonInput(1, input2);
      simulator.setAutomatonState(1, state2);

      // Perform 10 updates.
      for (int cnt = 1; cnt <= 10; cnt++) {
        simulator.update();
      }

      simulator.getAutomatonOutput(0, output);
      assertArrayEquals(new int[] {11, 12, 13, 14, 15}, output);

      simulator.getAutomatonOutput(1, output);
      assertArrayEquals(new int[] {20, 21, 22, 23, 14}, output);
    }
  }

  @Test
  public void stressTest_updates1000_automata500_size16() throws Exception {
    int numUpdates = 1000, numAutomata = 2, size = 16;
    stressTest(numUpdates, numAutomata, size);
  }

  private void stressTest(int numUpdates, int numAutomata, int size) throws Exception {
    int input[] = new int[size];
    int state[] = new int[size];
    int output[] = new int[size];
    for(int idx = 0; idx < size; idx++) {
      input[idx] = idx;
      state[idx] = 1;
    }

    assertEquals(size, input.length);
    assertEquals(size, state.length);
    assertEquals(size, output.length);
    try (OpenClCellularAutomatonSimulator simulation = new OpenClCellularAutomatonSimulator(
        numAutomata, size, size, size, numAutomata * size, size, 
            CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE)) {

      for(int idx = 0; idx < numAutomata; idx++) {
        simulation.setAutomatonState(idx, state);
      }

      for (int cnt = 1; cnt <= numUpdates; cnt++) {
        for(int idx = 0; idx < numAutomata; idx++) {
          simulation.setAutomatonInput(idx, input);
        }

        simulation.update();

        for(int idx = 0; idx < numAutomata; idx++) {
          simulation.getAutomatonOutput(0, output);
          assertEquals(output[0], output[1] - 1);
        }
      }
    }
  }

  @Override
  protected CellularAutomatonSimulator newSimulator(
      CellularAutomatonDescriptor descriptor, int automatonCount) throws Exception {
    return new OpenClCellularAutomatonSimulator(
        automatonCount, 16, 16, CLPlatform.getDefault().getMaxFlopsDevice(), descriptor);
  }
}
