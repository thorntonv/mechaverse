package org.mechaverse.simulation.common.cellautomaton.simulation.generator.java;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.AbstractCellularAutomatonSimulatorTest;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;

/**
 * Unit test for {@link JavaCellularAutomatonSimulator}.
 */
public class JavaCellularAutomatonSimulatorTest extends AbstractCellularAutomatonSimulatorTest {

  public static final int INPUT_SIZE = 4;
  public static final int OUTPUT_SIZE = 4;

  @Override
  protected CellularAutomatonSimulator newSimulator(
      CellularAutomatonDescriptor descriptor, int count) throws Exception {
    return new JavaCellularAutomatonSimulator(count, INPUT_SIZE, OUTPUT_SIZE, descriptor);
  }
}
