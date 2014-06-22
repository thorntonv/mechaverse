package org.mechaverse.simulation.common.opencl;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;
import com.jogamp.opencl.CLPlatform;

public class OpenClCircuitSimulationBenchmark extends Benchmark {

  @Param(value = {"500"}) int numCircuits;
  @Param(value = {"16"}) int size;

  private static final String KERNAL_SOURCE =
      "void kernel " + OpenClCircuitSimulation.KERNEL_NAME + "(" +
      "    global const int* input, global int* state, global int* output) {" +
      "  output[get_global_id(0)] = state[get_global_id(0)] + input[get_global_id(0)];" +
      "  state[get_global_id(0)]++;" +
      "}";

  private int[] input;
  private int[] state;
  private int[] output;
  private OpenClCircuitSimulation circuitSimulation;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    input = new int[size];
    state = new int[size];
    output = new int[size];
    circuitSimulation = new OpenClCircuitSimulation(numCircuits, size, size, size, 1, 1,
      CLPlatform.getDefault().getMaxFlopsDevice(), KERNAL_SOURCE);

    for(int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
      circuitSimulation.setState(circuitIdx, state);
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    circuitSimulation.close();
  }

  public int timeUpdate(int reps) throws Exception {
    int dummy = 0;
    for (int i = 0; i < reps; i++) {
      for (int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
        circuitSimulation.setInput(circuitIdx, input);
      }

      circuitSimulation.update();

      for (int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
        circuitSimulation.getOutput(circuitIdx, output);
        dummy += output[0];
      }
    }
    return dummy;
  }

  public static void main(String[] args) {
    CaliperMain.main(OpenClCircuitSimulationBenchmark.class, args);
  }
}
