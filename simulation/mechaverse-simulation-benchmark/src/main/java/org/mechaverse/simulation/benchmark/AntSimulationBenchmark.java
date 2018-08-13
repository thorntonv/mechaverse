package org.mechaverse.simulation.benchmark;

import java.util.concurrent.TimeUnit;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.common.Simulation;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Fork(value = 1, warmups = 0)
@Threads(1)
@Warmup(iterations = 3)
public class AntSimulationBenchmark {

  @State(Scope.Benchmark)
  public static class ExecutionPlan {

    @Param(value = {"500", "1024", "2048"}) int numAutomata;

    private ClassPathXmlApplicationContext appContext;
    private Simulation simulation;

    @Setup(Level.Trial)
    public void setUp() throws Exception {
      appContext =
          new ClassPathXmlApplicationContext("ant-simulation-context.xml");
      simulation = appContext.getBean(Simulation.class);
      AntSimulationModel model = (AntSimulationModel) simulation.generateRandomState();
      model.setAntMaxCount(numAutomata);
      simulation.setState(model);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
      appContext.close();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public int run(ExecutionPlan plan) {
    int dummy = 0;
    plan.simulation.step(1);
    return dummy;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(AntSimulationBenchmark.class.getSimpleName())
        .resultFormat(ResultFormatType.CSV)
        .build();
    new Runner(opt).run();
  }
}
