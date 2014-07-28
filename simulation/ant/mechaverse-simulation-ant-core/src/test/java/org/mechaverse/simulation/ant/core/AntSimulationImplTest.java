package org.mechaverse.simulation.ant.core;

import org.junit.Test;

/**
 * Unit test for {@link AntSimulationImpl}.
 */
public class AntSimulationImplTest {

  @Test
  public void simulate() {
    AntSimulationImpl simulation = new AntSimulationImpl();

    for (int cnt = 0; cnt < 10000; cnt++) {
      simulation.step();
    }
  }
}
