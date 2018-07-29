package org.mechaverse.simulation.benchmark;

import com.jogamp.opencl.CLPlatform;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulationUtil;
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
import org.openjdk.jmh.runner.RunnerException;

@Fork(value = 1, warmups = 0)
@Threads(1)
@Warmup(iterations = 3)
public class OpenClCellularAutomatonSimulationBenchmark {

  private static final String DESCRIPTOR_XML_FILENAME = "boolean4.xml";

  @State(Scope.Benchmark)
  public static class ExecutionPlan {

    @Param(value = {"175"}) int iterationsPerUpdate;
    @Param(value = {"1024"}) int numAutomata;
    @Param(value = {"16"}) int size;
    @Param(value = {"8"}) int width;
    @Param(value = {"8"}) int height;

    private int[] input;
    private int[] output;
    private OpenClCellularAutomatonSimulator simulator;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
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

    @TearDown(Level.Trial)
    public void tearDown() {
      simulator.close();
    }
  }


  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public int run(ExecutionPlan plan) {
    int dummy = 0;
    for (int idx = 0; idx < plan.numAutomata; idx++) {
      plan.simulator.setAutomatonInput(idx, plan.input);
    }
    plan.simulator.update();
    for (int idx = 0; idx < plan.numAutomata; idx++) {
      plan.simulator.getAutomatonOutput(idx, plan.output);
      dummy += plan.output[0];
    }
    return dummy;
  }

  public static void main(String[] args) throws IOException, RunnerException {
    org.openjdk.jmh.Main.main(args);
  }
}
