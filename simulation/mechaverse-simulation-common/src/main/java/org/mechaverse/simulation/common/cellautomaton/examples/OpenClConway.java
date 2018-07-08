package org.mechaverse.simulation.common.cellautomaton.examples;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;

import java.util.function.Function;

public class OpenClConway extends OpenClCellularAutomatonCLI {

  public static void main(String[] args) throws Exception {
    OpenClCellularAutomatonCLI.main(args, new OpenClConway());
  }

  @Override
  protected SimulatorCellularAutomaton createCellularAutomaton() throws IOException {
    SimulatorCellularAutomaton cells = super.createCellularAutomaton();

    int[] state = new int[cells.getSimulator().getAutomatonStateSize()];
    cells.setState(state);

    int col = cells.getWidth() / 2;
    int row = cells.getHeight() / 2;

    // R Pentomino
    cells.getCell(row, col).setOutput(0, 1);
    cells.getCell(row + 1, col).setOutput(0, 1);
    cells.getCell(row - 1, col).setOutput(0, 1);
    cells.getCell(row - 1, col + 1).setOutput(0, 1);
    cells.getCell(row, col - 1).setOutput(0, 1);
    return cells;
  }

  @Override
  protected InputStream getDescriptorInputStream() {
    return ClassLoader.getSystemResourceAsStream("conway.xml");
  }

  @Override
  protected Function<Cell, Color> getCellColorProvider() {
    return SINGLE_BITPLANE_CELL_COLOR_PROVIDER;
  }
}
