package org.mechaverse.simulation.common.opencl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.AbstractCircuitSimulatorTest;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;

import com.jogamp.opencl.CLPlatform;

/**
 * Unit test for {@link OpenClCircuitSimulator}.
 */
public class OpenClCircuitSimulatorTest extends AbstractCircuitSimulatorTest {

  private static final String KERNAL_SOURCE =
      "void kernel " + OpenClCircuitSimulator.KERNEL_NAME + "(" +
      "    global const int* input, global int* state, global int* outMap, global int* output, " +
      "        const unsigned int inputLength, const unsigned int outputLength) {" +
      "  output[get_global_id(0)] = state[get_global_id(0)] + input[get_global_id(0)];" +
      "  state[get_global_id(0)]++;" +
      "}";

  @Test
  public void update() throws Exception {
    int input[] = {10};
    int state[] = {5};
    int output[] = {0};

    try (OpenClCircuitSimulator circuitSimulation = new OpenClCircuitSimulator(
      1, 1, 1, 1, 1, 1, CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE)) {
        circuitSimulation.setCircuitInput(0, input);
        circuitSimulation.setCircuitState(0, state);
        circuitSimulation.update();
        circuitSimulation.getCircuitOutput(0, output);
        circuitSimulation.getCircuitState(0, state);

        assertEquals(15, output[0]);
        assertEquals(6, state[0]);
    }
  }

  @Test
  public void multipleUpdate() throws Exception {
    int input[] = {10};
    int state[] = {5};
    int output[] = {0};

    try (OpenClCircuitSimulator circuitSimulation = new OpenClCircuitSimulator(
      1, 1, 1, 1, 1, 1, CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE)) {
        circuitSimulation.setCircuitInput(0, input);
        circuitSimulation.setCircuitState(0, state);

      // Perform 10 updates.
      for (int cnt = 1; cnt <= 10; cnt++) {
        circuitSimulation.update();
      }

      circuitSimulation.getCircuitOutput(0, output);
      circuitSimulation.getCircuitState(0, state);

      assertEquals(5+9+10, output[0]);
      assertEquals(15, state[0]);
    }
  }

  @Test
  public void multipleCircuits() throws Exception {
    // Two circuits each with 5 elements.
    int circuitInput1[] = {1, 2, 3, 4, 5};
    int circuitState1[] = {1, 1, 1, 1, 1};
    int circuitInput2[] = {6, 7, 8, 9, 0};
    int circuitState2[] = {5, 5, 5, 5, 5};
    int circuitOutput[] = {0, 0, 0, 0, 0};

    try (OpenClCircuitSimulator circuitSimulation = new OpenClCircuitSimulator(
      2, 5, 5, 5, 10, 5, CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE)) {

      assertEquals(5, circuitSimulation.getCircuitInputSize());
      assertEquals(5, circuitSimulation.getCircuitStateSize());
      assertEquals(5, circuitSimulation.getCircuitOutputSize());
      circuitSimulation.setCircuitInput(0, circuitInput1);
      circuitSimulation.setCircuitState(0, circuitState1);
      circuitSimulation.setCircuitInput(1, circuitInput2);
      circuitSimulation.setCircuitState(1, circuitState2);

      // Perform 10 updates.
      for (int cnt = 1; cnt <= 10; cnt++) {
        circuitSimulation.update();
      }

      circuitSimulation.getCircuitOutput(0, circuitOutput);
      assertArrayEquals(new int[] {11, 12, 13, 14, 15}, circuitOutput);

      circuitSimulation.getCircuitOutput(1, circuitOutput);
      assertArrayEquals(new int[] {20, 21, 22, 23, 14}, circuitOutput);
    }
  }

  @Test
  public void stressTest_updates1000_circuits500_size16() throws Exception {
    int numUpdates = 1000, numCircuits = 2, size = 16;
    stressTest(numUpdates, numCircuits, size);
  }

  private void stressTest(int numUpdates, int numCircuits, int size) throws Exception {
    int circuitInput[] = new int[size];
    int circuitState[] = new int[size];
    int circuitOutput[] = new int[size];
    for(int idx = 0; idx < size; idx++) {
      circuitInput[idx] = idx;
      circuitState[idx] = 1;
    }

    assertEquals(size, circuitInput.length);
    assertEquals(size, circuitState.length);
    assertEquals(size, circuitOutput.length);
    try (OpenClCircuitSimulator circuitSimulation = new OpenClCircuitSimulator(
        numCircuits, size, size, size, numCircuits*size, size,
            CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE)) {

      for(int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
        circuitSimulation.setCircuitState(circuitIdx, circuitState);
      }

      for (int cnt = 1; cnt <= numUpdates; cnt++) {
        for(int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
          circuitSimulation.setCircuitInput(circuitIdx, circuitInput);
        }

        circuitSimulation.update();

        for(int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
          circuitSimulation.getCircuitOutput(0, circuitOutput);
          assertEquals(circuitOutput[0], circuitOutput[1] - 1);
        }
      }
    }
  }

  @Override
  protected CircuitSimulator newCircuitSimulator(Circuit circuit, int circuitCount)
      throws Exception {
    return new OpenClCircuitSimulator(
      circuitCount, 16, 16, CLPlatform.getDefault().getMaxFlopsDevice(), circuit);
  }
}
