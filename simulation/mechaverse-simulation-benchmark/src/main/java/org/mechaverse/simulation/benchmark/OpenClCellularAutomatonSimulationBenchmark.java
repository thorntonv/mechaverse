package org.mechaverse.simulation.benchmark;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;
import com.jogamp.opencl.CLPlatform;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulationUtil;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;

public class OpenClCellularAutomatonSimulationBenchmark extends Benchmark {

  private static final String DESCRIPTOR_XML_FILENAME = "boolean4.xml";
  
  @Param(value = {"175"}) int iterationsPerUpdate;
  @Param(value = {"1024"}) int numAutomata;
  @Param(value = {"16"}) int size;
  @Param(value = {"8"}) int width;
  @Param(value = {"8"}) int height;

  private int[] input;
  private int[] output;
  private OpenClCellularAutomatonSimulator simulator;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    final RandomGenerator random = new Well19937c();

    CellularAutomatonDescriptor descriptor = CellularAutomatonDescriptorReader.read(
        ClassLoader.getSystemResourceAsStream(DESCRIPTOR_XML_FILENAME));
    descriptor.setIterationsPerUpdate(iterationsPerUpdate);
    descriptor.setWidth(width);
    descriptor.setHeight(height);

    try {
      simulator = new OpenClCellularAutomatonSimulator(numAutomata, size, size, 
          CLPlatform.getDefault().getMaxFlopsDevice(), descriptor);

      input = CellularAutomatonSimulationUtil.randomState(size, random);
      output = CellularAutomatonSimulationUtil.randomState(size, random);
      int[] ioMap = new int[size];
      for (int idx = 0; idx < size; idx++) {
        ioMap[idx] = random.nextInt(simulator.getAutomatonStateSize());
      }

      for (int idx = 0; idx < numAutomata; idx++) {
        int[] state = CellularAutomatonSimulationUtil.randomState(
            simulator.getAutomatonStateSize(), random);
        simulator.setAutomatonState(idx, state);
        simulator.setAutomatonInputMap(idx, ioMap);
        simulator.setAutomatonOutputMap(idx, ioMap);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    simulator.close();
  }

  @SuppressWarnings("unused")
  public int timeUpdate(int reps) {
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
