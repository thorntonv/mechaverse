package org.mechaverse.simulation.benchmark;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitReader;
import org.mechaverse.simulation.common.opencl.OpenClCircuitSimulator;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;
import com.jogamp.opencl.CLPlatform;

public class OpenClCircuitSimulationBenchmark extends Benchmark {

  @Param(value = {"200"}) int iterationsPerUpdate;
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
    output = new int[size];

    Circuit circuit = CircuitReader.read(ClassLoader.getSystemResourceAsStream("circuit.xml"));
    circuit.setIterationsPerUpdate(iterationsPerUpdate);
    circuitSimulation = new OpenClCircuitSimulator(
        numCircuits, size, size, CLPlatform.getDefault().getMaxFlopsDevice(), circuit);

    state = new int[circuitSimulation.getCircuitStateSize()];
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
      for (int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
        circuitSimulation.setCircuitInput(circuitIdx, input);
      }
      circuitSimulation.update();
      for (int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
        circuitSimulation.getCircuitOutput(circuitIdx, output);
        dummy += output[0];
      }
    }
    return dummy;
  }

  public static void main(String[] args) {
    CaliperMain.main(OpenClCircuitSimulationBenchmark.class, args);
  }
}
