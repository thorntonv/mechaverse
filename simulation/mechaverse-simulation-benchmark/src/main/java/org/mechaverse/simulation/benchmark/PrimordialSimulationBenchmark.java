package org.mechaverse.simulation.benchmark;

import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationModelGenerator;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
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

@Fork(value = 1, warmups = 0, jvmArgs = {"-Xmx10G", "-server"})
@Threads(1)
@Warmup(iterations = 1)
public class PrimordialSimulationBenchmark {

  @State(Scope.Benchmark)
  public static class ExecutionPlan {

    @Param(value = {"31"}) int subEnvironmentCount;
    @Param(value = {"1048576"}) int numEntities;
    @Param(value = {"25"}) int stepCount;

    private ClassPathXmlApplicationContext appContext;
    private Simulation simulation;

    @Setup(Level.Trial)
    public void setUp() throws Exception {
      appContext =
          new ClassPathXmlApplicationContext("primordial-simulation-context.xml");
      simulation = appContext.getBean(Simulation.class);

      PrimordialSimulationModelGenerator modelGenerator =
          new PrimordialSimulationModelGenerator(subEnvironmentCount);
      PrimordialSimulationModel model = modelGenerator.generate(new Well19937c());
      model.setEntityMaxCountPerEnvironment(numEntities / (subEnvironmentCount + 1));
      simulation.setState(model);
      for(int cnt = 1; cnt <= 1000/stepCount; cnt++) {
        simulation.step(stepCount);
      }
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
    plan.simulation.step(plan.stepCount);
    return dummy;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(PrimordialSimulationBenchmark.class.getSimpleName())
        .resultFormat(ResultFormatType.CSV)
        .build();
    new Runner(opt).run();
  }
}
