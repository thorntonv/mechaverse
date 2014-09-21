package org.mechaverse.simulation.benchmark;

import org.mechaverse.simulation.ant.core.AntSimulation;

import com.google.caliper.Benchmark;
import com.google.caliper.runner.CaliperMain;

public class AntSimulationBenchmark extends Benchmark {

  private AntSimulation simulation = new AntSimulation();

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    simulation = new AntSimulation();

    for(int cnt = 0; cnt < 10000; cnt++) {
      simulation.step();
    }
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
