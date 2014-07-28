package org.mechaverse.simulation.benchmark;

import org.mechaverse.simulation.ant.core.AntSimulationImpl;

import com.google.caliper.Benchmark;
import com.google.caliper.runner.CaliperMain;

public class AntSimulationBenchmark extends Benchmark {

  private AntSimulationImpl simulation = new AntSimulationImpl();

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    simulation = new AntSimulationImpl();
  }

  public int timeUpdate(int reps) throws Exception {
    for (int i = 0; i < reps; i++) {
      simulation.step();
    }
    return 0;
  }

  public static void main(String[] args) throws InterruptedException {
    CaliperMain.main(AntSimulationBenchmark.class, args);
  }
}
