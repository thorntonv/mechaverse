package org.mechaverse.simulation.benchmark;

import com.jogamp.opencl.CLPlatform;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.BitwiseCellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulationUtil;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Fork(value = 1, warmups = 0, jvmArgs = {"-Xmx10G", "-server"})
@Threads(1)
@Warmup(iterations = 1)
public class OpenClCellularAutomatonSimulationBenchmark {

  private static final String DESCRIPTOR_XML_FILENAME = "boolean4.xml";

  @State(Scope.Benchmark)
  public static class ExecutionPlan {

    @Param(value = {"20"}) int iterationsPerUpdate;
    @Param(value = {"262144"}) int numAutomata;
    @Param(value = {"6"}) int inputSize;
    @Param(value = {"4"}) int outputSize;
    @Param(value = {"2"}) int width;
    @Param(value = {"2"}) int height;
    @Param(value = {"25"}) int stepCount;

    private int[] input;
    private int[] output;
    private CellularAutomatonSimulator simulator;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
      final RandomGenerator random = new Well19937c();

      CellularAutomatonDescriptor descriptor = CellularAutomatonDescriptorReader.read(
          ClassLoader.getSystemResourceAsStream(DESCRIPTOR_XML_FILENAME));
      descriptor.setIterationsPerUpdate(iterationsPerUpdate);
      descriptor.setWidth(width);
      descriptor.setHeight(height);
      CellularAutomatonSimulationModel model = CellularAutomatonSimulationModelBuilder.build(descriptor);

      try {
        simulator = new OpenClCellularAutomatonSimulator(numAutomata / Integer.SIZE, inputSize, outputSize,
            CLPlatform.getDefault().getMaxFlopsDevice(), descriptor);

        simulator = new BitwiseCellularAutomatonSimulator(simulator, 1);

        input = CellularAutomatonSimulationUtil.randomState(simulator.getAutomatonInputSize(), random);
        output = CellularAutomatonSimulationUtil.randomState(simulator.getAutomatonOutputSize(), random);
        int[] inputMap = new int[simulator.getAutomatonInputMapSize()];
        for (int idx = 0; idx < inputMap.length; idx++) {
          inputMap[idx] = random.nextInt(simulator.getAutomatonStateSize());
        }
        int[] outputMap = new int[simulator.getAutomatonOutputMapSize()];
        for (int idx = 0; idx < inputMap.length; idx++) {
          inputMap[idx] = random.nextInt(model.getCellOutputStateSize());
        }

        for (int idx = 0; idx < simulator.size(); idx++) {
          int[] state = CellularAutomatonSimulationUtil.randomState(
              simulator.getAutomatonStateSize(), random);
          simulator.setAutomatonState(idx, state);
          simulator.setAutomatonInputMap(idx, inputMap);
          simulator.setAutomatonOutputMap(idx, outputMap);
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
      simulator.close();
    }
  }


  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public int run(ExecutionPlan plan) {
    int dummy = 0;
    for (int cnt = 1; cnt <= plan.stepCount; cnt++) {
      for (int idx = 0; idx < plan.simulator.size(); idx++) {
        plan.simulator.setAutomatonInput(idx, plan.input);
      }
      plan.simulator.update();
      for (int idx = 0; idx < plan.simulator.size(); idx++) {
        plan.simulator.getAutomatonOutput(idx, plan.output);
        dummy += plan.output[0];
      }
    }
    return dummy;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(OpenClCellularAutomatonSimulationBenchmark.class.getSimpleName())
        .resultFormat(ResultFormatType.CSV)
        .build();
    new Runner(opt).run();
  }
}
