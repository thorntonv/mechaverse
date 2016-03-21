package org.mechaverse.simulation.common.cellautomaton.examples;

import com.google.common.base.Function;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.FloatOpenClCellularAutomatonSimulator;

import java.awt.*;
import java.io.IOException;

/**
 * A base class for OpenCL based cellular automata visualization command line applications.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class FloatOpenClCellularAutomatonCLI extends OpenClCellularAutomatonCLI {

  protected static final Function<Cell, Color> FLOAT_COLOR_PROVIDER =
      new Function<Cell, Color>() {
        @Override
        public Color apply(Cell cell) {
          int color = (int) ((float) Math.abs(cell.getOutput(0)) / Integer.MAX_VALUE * 255.0f);
          return new Color(color, color, color);
        }
      };

  protected static void main(String[] args, FloatOpenClCellularAutomatonCLI cli) throws IOException {
    OpenClCellularAutomatonCLI.main(args, cli);
  }

  protected CellularAutomatonSimulator createSimulator(CellularAutomatonDescriptor descriptor) {
    return new FloatOpenClCellularAutomatonSimulator(
        new CellularAutomatonSimulatorConfig.Builder()
            .setDescriptor(descriptor)
            .build());
  }
}
