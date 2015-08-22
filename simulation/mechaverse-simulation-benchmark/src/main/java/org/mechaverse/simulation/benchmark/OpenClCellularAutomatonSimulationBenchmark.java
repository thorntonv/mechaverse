package org.mechaverse.simulation.benchmark;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulationUtil;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;
import com.google.monitoring.runtime.instrumentation.common.com.google.common.io.Files;
import com.jogamp.opencl.CLPlatform;

public class OpenClCellularAutomatonSimulationBenchmark extends Benchmark {

  private static final String DESCRIPTOR_XML_FILENAME = "boolean4.xml";
  
  @Param(value = {"175"}) int iterationsPerUpdate;
  @Param(value = {"528"}) int numAutomata;
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
        ClassLoader.getSystemResourceAsStream(DESCRIPTOR_XML_FILENAME));
    descriptor.setIterationsPerUpdate(iterationsPerUpdate);
    descriptor.setWidth(width);
    descriptor.setHeight(height);

    try {
      simulator = new OpenClCellularAutomatonSimulator(numAutomata, size, size, 
          CLPlatform.getDefault().getMaxFlopsDevice(), descriptor);

//      simulator = getSimulator(new File("boolean4.ocl"), numAutomata, size,
//        new CellularAutomatonSimulationModelBuilder().buildModel(descriptor));
    } catch (Throwable t) {
      t.printStackTrace();
    }

    state = CellularAutomatonSimulationUtil.randomState(
        simulator.getAutomatonStateSize(), new Well19937c());
    for (int idx = 0; idx < numAutomata; idx++) {
      simulator.setAutomatonState(idx, state);
    }
  }

  protected static OpenClCellularAutomatonSimulator getSimulator(File kernelSrcFile, 
      int numAutomata, int size, CellularAutomatonSimulationModel model) throws IOException {
    String kernelSource = Files.toString(kernelSrcFile, Charset.defaultCharset());
    return new OpenClCellularAutomatonSimulator(numAutomata, size, model.getStateSize(), size,
        numAutomata * model.getLogicalUnitCount(), model.getLogicalUnitCount(), 
            CLPlatform.getDefault().getMaxFlopsDevice(), kernelSource);
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    simulator.close();
  }

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
