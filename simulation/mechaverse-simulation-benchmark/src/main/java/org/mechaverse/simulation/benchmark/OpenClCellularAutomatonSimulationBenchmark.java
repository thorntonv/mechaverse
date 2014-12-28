package org.mechaverse.simulation.benchmark;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;
import com.jogamp.opencl.CLPlatform;

public class OpenClCellularAutomatonSimulationBenchmark extends Benchmark {

  @Param(value = {"200"}) int iterationsPerUpdate;
  @Param(value = {"500"}) int numAutomata;
  @Param(value = {"16"}) int size;
  @Param(value = {"16"}) int width;
  @Param(value = {"16"}) int height;

  private int[] input;
  private int[] state;
  private int[] output;
  private OpenClCellularAutomatonSimulator simulator;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    input = new int[size];
    output = new int[size];

    CellularAutomatonDescriptor descriptor = CellularAutomatonDescriptorReader.read(
        ClassLoader.getSystemResourceAsStream("boolean3.xml"));
    descriptor.setIterationsPerUpdate(iterationsPerUpdate);
    descriptor.setWidth(height);
    descriptor.setHeight(height);

    simulator = new OpenClCellularAutomatonSimulator(
        numAutomata, size, size, CLPlatform.getDefault().getMaxFlopsDevice(), descriptor);

    state = new int[simulator.getAutomatonStateSize()];
    for (int idx = 0; idx < numAutomata; idx++) {
      simulator.setAutomatonState(idx, state);
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    simulator.close();
  }

  public int timeUpdate(int reps) throws Exception {
    int dummy = 0;
    for (int i = 0; i < reps; i++) {
      for (int idx = 0; idx < numAutomata; idx++) {
        simulator.setAutomatonInput(idx, input);
      }
      simulator.update();
      for (int idx = 0; idx < numAutomata; idx++) {
        simulator.getAutomatonOutput(idx, output);
        dummy += output[0];
      }
    }
    return dummy;
  }

  public static void main(String[] args) {
    CaliperMain.main(OpenClCellularAutomatonSimulationBenchmark.class, args);
  }
}
