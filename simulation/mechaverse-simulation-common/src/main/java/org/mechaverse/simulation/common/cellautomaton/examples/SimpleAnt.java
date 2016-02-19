package org.mechaverse.simulation.common.cellautomaton.examples;

import com.google.common.base.Function;
import com.google.common.io.Resources;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;
import org.mechaverse.simulation.common.util.ArrayUtil;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class SimpleAnt extends OpenClCellularAutomatonCLI {

  public static void initCellularAutomaton(final SimulatorCellularAutomaton cellularAutomaton) {
    cellularAutomaton.getCell(0, 0).addOutputToInputMap(0);
    cellularAutomaton.getCell(0, cellularAutomaton.getWidth() / 2).addOutputToInputMap(0);
    cellularAutomaton.getCell(0, cellularAutomaton.getWidth() - 1).addOutputToInputMap(0);

    cellularAutomaton.getCell(cellularAutomaton.getHeight() - 1, 0).addOutputToOutputMap(0);
    cellularAutomaton.getCell(cellularAutomaton.getHeight() - 1, cellularAutomaton.getWidth() - 1)
        .addOutputToOutputMap(0);

    cellularAutomaton.updateInputMap();
    cellularAutomaton.updateOutputMap();
  }

  public static void main(String[] args) throws Exception {
    OpenClCellularAutomatonCLI.main(args, new SimpleAnt());
  }

  @Override
  protected SimulatorCellularAutomaton createCellularAutomaton() throws IOException {
    SimulatorCellularAutomaton cells = super.createCellularAutomaton();
    int[] state = ArrayUtil.toIntArray(Resources.toByteArray(
        Resources.getResource("simple-ant.dat")));
    if (state.length != cells.getSimulator().getAutomatonStateSize()) {
      throw new IOException(String.format("Unexpected state size. expected: %d, actual: %d",
          cells.getSimulator().getAutomatonStateSize(), state.length));
    }
    cells.setState(state);
    initCellularAutomaton(cells);

    // Front left food. Turn counterclockwise.
    cells.getCell(0, 0).setOutput(0, 1);
    // Front center food. No turn.
    cells.getCell(0, cells.getWidth() / 2).setOutput(0, 0);
    // Front right food. Turn clockwise.
    cells.getCell(0, cells.getWidth() - 1).setOutput(0, 1);
    cells.updateInputs();

    return cells;
  }

  protected CellularAutomatonSimulator createSimulator(CellularAutomatonDescriptor descriptor) {
    descriptor.setIterationsPerUpdate(1);
    return new OpenClCellularAutomatonSimulator(
        new CellularAutomatonSimulatorConfig.Builder()
            .setDescriptor(descriptor)
            .setAutomatonInputSize(3)
            .setAutomatonOutputSize(2)
            .build());
  }

  @Override
  protected InputStream getDescriptorInputStream() {
    return ClassLoader.getSystemResourceAsStream("boolean4.xml");
  }

  @Override
  protected Function<CellularAutomaton.Cell, Color> getCellColorProvider() {
    return SINGLE_BITPLANE_CELL_COLOR_PROVIDER;
  }
}
