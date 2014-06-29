package org.mechaverse.simulation.common.opencl;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitTestUtil;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;
import com.jogamp.opencl.CLPlatform;

public class OpenClCircuitSimulationBenchmark extends Benchmark {

  @Param(value = {"8", "12", "16"}) int numElements;
  @Param(value = {"128", "256"}) int numLogicalUnits;
  @Param(value = {"200"}) int iterationsPerUpdate;
  @Param(value = {"false"}) boolean ioEnabled;
  @Param(value = {"500"}) int numCircuits;
  @Param(value = {"16"}) int size;

  private int[] input;
  private int[] state;
  private int[] output;
  private OpenClCircuitSimulator circuitSimulation;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    input = new int[size];
    state = new int[size];
    output = new int[size];

    Circuit circuit = CircuitTestUtil.createTestCircuit(numLogicalUnits, 1,
        CircuitTestUtil.createRouting3in3OutElementType(), 1, numElements);
    circuitSimulation = new OpenClCircuitSimulator(numCircuits, size, size,
        CLPlatform.getDefault().getMaxFlopsDevice(), circuit);

    for (int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
      circuitSimulation.setCircuitState(circuitIdx, state);
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
      if (ioEnabled) {
        for (int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
          circuitSimulation.setCircuitInput(circuitIdx, input);
        }
      }
      circuitSimulation.update();
      if (ioEnabled) {
        for (int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
          circuitSimulation.getCircuitOutput(circuitIdx, output);
          dummy += output[0];
        }
      }
    }
    return dummy;
  }

  public static void main(String[] args) {
    CaliperMain.main(OpenClCircuitSimulationBenchmark.class, args);
  }
}
