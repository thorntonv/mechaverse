package org.mechaverse.simulation.common.cellautomaton.examples;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.analysis.CellularAutomatonAnalyzer;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulationUtil;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonRenderer;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonVisualizer;

import com.google.common.base.Function;

/**
 * A base class for OpenCL based cellular automata visualization command line applications.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class OpenClCellularAutomatonCLI extends CellularAutomatonCLI {

  protected static final Function<Cell, Color> SINGLE_BITPLANE_CELL_COLOR_PROVIDER =
      new Function<Cell, Color>() {
        @Override
        public Color apply(Cell cell) {
          return ((cell.getOutput(0) >> 0) & 0b1) == 1 ? Color.WHITE : Color.BLACK;
        }
      };

  protected static final Function<Cell, Color> BITPLANE_AVERAGE_CELL_COLOR_PROVIDER =
      new Function<Cell, Color>() {
        @Override
        public Color apply(Cell cell) {
          int color = CellularAutomatonAnalyzer.getSetBitCount(cell.getOutput(0)) * 8;
          return new Color(color, color, color);
        }
      };

  protected static void main(String[] args, OpenClCellularAutomatonCLI cli) throws IOException {
    CellularAutomatonCLI.main(args, cli);
  }

  protected abstract InputStream getDescriptorInputStream();

  protected abstract Function<Cell, Color> getCellColorProvider();

  @Override
  protected SimulatorCellularAutomaton createCellularAutomaton() throws IOException {
    CellularAutomatonDescriptor descriptor = getDescriptor();

    CellularAutomatonSimulator simulator = new OpenClCellularAutomatonSimulator(
        new CellularAutomatonSimulatorConfig.Builder()
            .setDescriptor(descriptor)
            .build());
    SimulatorCellularAutomaton cells = new SimulatorCellularAutomaton(descriptor, simulator);
    int[] state = CellularAutomatonSimulationUtil.randomState(
        simulator.getAutomatonStateSize(), new Well19937c());
    cells.setState(state);
    return cells;
  }

  protected CellularAutomatonDescriptor getDescriptor() throws IOException {
    return CellularAutomatonDescriptorReader.read(getDescriptorInputStream());
  }

  @Override
  protected CellularAutomatonRenderer createCellularAutomatonRenderer(CellularAutomaton cells,
      int width, int height) {
    return new CellularAutomatonRenderer(cells, getCellColorProvider(), width, height);
  }

  @Override
  protected CellularAutomatonVisualizer createVisualizer(int width, int height, int framesPerSecond)
      throws IOException {
    return new CellularAutomatonVisualizer(createCellularAutomaton(), getCellColorProvider(),
        width, height, framesPerSecond);
  }
}
