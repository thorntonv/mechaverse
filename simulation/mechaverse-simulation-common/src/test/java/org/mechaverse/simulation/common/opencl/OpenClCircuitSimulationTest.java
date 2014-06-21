package org.mechaverse.simulation.common.opencl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jogamp.opencl.CLPlatform;

public class OpenClCircuitSimulationTest {

  private static final String KERNAL_SOURCE =
      "void kernel " + OpenClCircuitSimulation.KERNEL_NAME + "(" +
      "    global const int* input, global int* state, global int* output) {" +
      "  output[get_global_id(0)] = state[get_global_id(0)] + input[get_global_id(0)];" +
      "  state[get_global_id(0)]++;" +
      "}";

  @Test
  public void update() throws Exception {
    int input[] = {10};
    int state[] = {5};
    int output[] = {0};

    try (OpenClCircuitSimulation circuitSimulation = new OpenClCircuitSimulation(
      1, 1, 1, 1, 1, 1, CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE)) {
        circuitSimulation.setInput(0, input);
        circuitSimulation.setState(0, state);
        circuitSimulation.update();
        circuitSimulation.getOutput(0, output);
        circuitSimulation.getState(0, state);

        assertEquals(15, output[0]);
        assertEquals(6, state[0]);
    }
  }

  @Test
  public void multipleUpdate() throws Exception {
    int input[] = {10};
    int state[] = {5};
    int output[] = {0};

    try (OpenClCircuitSimulation circuitSimulation = new OpenClCircuitSimulation(
      1, 1, 1, 1, 1, 1, CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE)) {
        circuitSimulation.setInput(0, input);
        circuitSimulation.setState(0, state);

      // Perform 10 updates.
      for (int cnt = 1; cnt <= 10; cnt++) {
        circuitSimulation.update();
      }

      circuitSimulation.getOutput(0, output);
      circuitSimulation.getState(0, state);

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

    try (OpenClCircuitSimulation circuitSimulation = new OpenClCircuitSimulation(
      2, 5, 5, 5, 10, 1, CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE)) {

      circuitSimulation.setInput(0, circuitInput1);
      circuitSimulation.setState(0, circuitState1);
      circuitSimulation.setInput(1, circuitInput2);
      circuitSimulation.setState(1, circuitState2);

      // Perform 10 updates.
      for (int cnt = 1; cnt <= 10; cnt++) {
        circuitSimulation.update();
      }

      circuitSimulation.getOutput(0, circuitOutput);
      assertArrayEquals(new int[] {11, 12, 13, 14, 15}, circuitOutput);

      circuitSimulation.getOutput(1, circuitOutput);
      assertArrayEquals(new int[] {20, 21, 22, 23, 14}, circuitOutput);
    }
  }
}
