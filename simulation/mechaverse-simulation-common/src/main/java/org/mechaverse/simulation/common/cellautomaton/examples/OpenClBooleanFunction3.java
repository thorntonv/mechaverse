package org.mechaverse.simulation.common.cellautomaton.examples;

import java.awt.Color;
import java.io.InputStream;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;

import java.util.function.Function;

/**
 * Three input boolean function implementation of {@link OpenClCellularAutomatonCLI}.
 */
public class OpenClBooleanFunction3 extends OpenClCellularAutomatonCLI {
  
  public static void main(String[] args) throws Exception {
    OpenClCellularAutomatonCLI.main(args, new OpenClBooleanFunction3());
  }

  @Override
  protected InputStream getDescriptorInputStream() {
    return ClassLoader.getSystemResourceAsStream("boolean3.xml");
  }

  @Override
  protected Function<Cell, Color> getCellColorProvider() {
    return SINGLE_BITPLANE_CELL_COLOR_PROVIDER;
  }
}