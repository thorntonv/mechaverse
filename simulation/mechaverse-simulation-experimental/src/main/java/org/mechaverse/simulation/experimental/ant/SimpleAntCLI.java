package org.mechaverse.simulation.experimental.ant;

import java.util.function.Function;
import com.google.common.io.Resources;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.examples.OpenClCellularAutomatonCLI;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;
import org.mechaverse.simulation.common.util.ArrayUtil;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class SimpleAntCLI extends OpenClCellularAutomatonCLI {

  public static void main(String[] args) throws Exception {
    OpenClCellularAutomatonCLI.main(args, new SimpleAntCLI());
  }

  @Override
  protected SimulatorCellularAutomaton createCellularAutomaton() throws IOException {
    final SimulatorCellularAutomaton cells = super.createCellularAutomaton();
    int[] state = ArrayUtil.toIntArray(Resources.toByteArray(
        Resources.getResource("simple-ant.dat")));
    if (state.length != cells.getSimulator().getAutomatonStateSize()) {
      throw new IOException(String.format("Unexpected state size. expected: %d, actual: %d",
          cells.getSimulator().getAutomatonStateSize(), state.length));
    }
    SimpleAntEntity.initCellularAutomaton(cells);
    cells.setState(state);

    init(cells);

    final SimpleAntEntity.Output antOutput = new SimpleAntEntity.Output();
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        cells.refreshOutputs();
        antOutput.setData(cells.getOutputs());
        System.out.println(antOutput.getTurnDirection());
      }
    }, 0, 1000);

    return cells;
  }

  public static void init(SimulatorCellularAutomaton cells) {
    SimpleAntEntity entity = new SimpleAntEntity();
    entity.setCellularAutomaton(cells);

    // Perform a number of updates to warm up.
    for (int cnt = 1; cnt < 200; cnt++) {
      setInputs(cells, entity.getInput());
      cells.updateInputs();
      for(int idx = 0; idx < 20; idx++) {
        cells.update();
        cells.updateInputs();
      }
      cells.refreshOutputs();
      entity.processOutput(cells.getOutputs());
    }
    System.out.println("Fitness: "+ entity.getFitness());
  }

  public static void setInputs(final SimulatorCellularAutomaton cellularAutomaton, final int[] input) {
    // Front left food. Turn counterclockwise.
    cellularAutomaton.getCell(0, 0).setOutput(0, input[0]);
    // Front center food. No turn.
    cellularAutomaton.getCell(0, cellularAutomaton.getWidth() / 2).setOutput(0, input[1]);
    // Front right food. Turn clockwise.
    cellularAutomaton.getCell(0, cellularAutomaton.getWidth() - 1).setOutput(0, input[2]);
  }

  protected CellularAutomatonSimulator createSimulator(CellularAutomatonDescriptor descriptor) {
    descriptor.setIterationsPerUpdate(1);
    return new OpenClCellularAutomatonSimulator(
        new CellularAutomatonSimulatorConfig.Builder()
            .setDescriptor(descriptor)
            .setAutomatonInputSize(SimpleAntEntity.Input.DATA_SIZE)
            .setAutomatonOutputSize(SimpleAntEntity.Output.DATA_SIZE)
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
