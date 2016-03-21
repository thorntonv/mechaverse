package org.mechaverse.simulation.common.cellautomaton.examples;

import com.google.common.base.Function;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;

import java.awt.*;
import java.io.InputStream;

/**
 * Weighted average implementation of {@link OpenClCellularAutomatonCLI}.
 */
public class OpenClWeightedAverage4 extends FloatOpenClCellularAutomatonCLI {
  
  public static void main(String[] args) throws Exception {
    FloatOpenClCellularAutomatonCLI.main(args, new OpenClWeightedAverage4());
  }

  @Override
  protected InputStream getDescriptorInputStream() {
    return ClassLoader.getSystemResourceAsStream("weighted-average4.xml");
  }

  @Override
  protected Function<Cell, Color> getCellColorProvider() {
    return FLOAT_COLOR_PROVIDER;
  }
}
