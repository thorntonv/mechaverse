package org.mechaverse.simulation.common.cellautomaton.examples;

import java.awt.Color;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonVisualizer;

import com.google.common.base.Function;

public class Conway {

  private static final Function<Cell, Color> CELL_COLOR_PROVIDER = new Function<Cell, Color>() {
    @Override
    public Color apply(Cell cell) {
      return cell.getOutput(0) == 1 ? Color.WHITE : Color.BLACK;
    }
  };

  public static void main(String[] args) throws Exception {
    CellularAutomatonDescriptor descriptor = CellularAutomatonDescriptorReader.read(
        ClassLoader.getSystemResourceAsStream("conway.xml"));
    OpenClCellularAutomatonSimulator simulator =
        new OpenClCellularAutomatonSimulator(1, 1, 1, descriptor);

    CellularAutomaton cells = new SimulatorCellularAutomaton(descriptor, simulator);
    CellularAutomatonVisualizer visualizer = 
        new CellularAutomatonVisualizer(cells, CELL_COLOR_PROVIDER);
    
    int col = cells.getWidth() / 2;
    int row = cells.getHeight() / 2;

    // R Pentomino
    cells.getCell(row, col).setOutput(0, 1);
    cells.getCell(row + 1, col).setOutput(0, 1);
    cells.getCell(row - 1, col).setOutput(0, 1);
    cells.getCell(row - 1, col + 1).setOutput(0, 1);
    cells.getCell(row, col - 1).setOutput(0, 1);
    
    visualizer.start();
  }
}
