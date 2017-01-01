package org.mechaverse.simulation.primordial.core;

import com.google.common.base.Function;
import org.mechaverse.simulation.common.cellautomaton.examples.CellularAutomatonCLI;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonRenderer;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonVisualizer;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.awt.*;
import java.io.IOException;

public class PrimordialCellularAutomaton implements CellularAutomaton {

  private static final class CellAdapter implements Cell {

    private org.mechaverse.simulation.primordial.core.Cell primordialCell;

    public CellAdapter(org.mechaverse.simulation.primordial.core.Cell primordialCell) {
      this.primordialCell = primordialCell;
    }

    @Override
    public int getOutput(int idx) {
      if (primordialCell.getEntities().isEmpty() || primordialCell.getEntities().get(idx) == null) {
        return -1;
      }
      return EntityUtil.getType(primordialCell.getEntities().get(idx)).ordinal();
    }

    @Override
    public void setOutput(int idx, int value) {
    }

    @Override
    public int getOutputCount() {
      return primordialCell.getEntities().size();
    }
  }


  private PrimordialSimulationImpl simulation;
  private CellEnvironment cellEnvironment;

  public PrimordialCellularAutomaton(PrimordialSimulationImpl simulation) {
    this.simulation = simulation;
  }

  @Override
  public int getWidth() {
    return getCellEnvironment().getColumnCount();
  }

  @Override
  public int getHeight() {
    return getCellEnvironment().getRowCount();
  }

  @Override
  public Cell getCell(int row, int column) {
    return new CellAdapter(getCellEnvironment().getCell(row, column));
  }

  private CellEnvironment getCellEnvironment() {
    if (cellEnvironment == null) {
      cellEnvironment = simulation.getEnvironmentSimulations().get(0).getEnvironment();
    }
    return cellEnvironment;
  }

  @Override
  public void update() {
    simulation.step();
  }

  @Override
  public void updateInputs() {
  }

  private static final Function<Cell, Color> CELL_COLOR_PROVIDER = new Function<Cell, Color>() {
    @Override
    public Color apply(Cell cell) {
      if (cell.getOutput(0) == EntityType.ENTITY.ordinal()) {
        return Color.WHITE;
      }
      if (cell.getOutput(0) == EntityType.FOOD.ordinal()) {
        return Color.GREEN;
      }
      return Color.BLACK;
    }
  };

  public static void main(String[] args) throws IOException {

    final PrimordialSimulationImpl simulation;
    try (ClassPathXmlApplicationContext context =
             new ClassPathXmlApplicationContext("simulation-context.xml")) {
      simulation = context.getBean(PrimordialSimulationImpl.class);
      simulation.setState(simulation.generateRandomState());

      CellularAutomatonCLI cli = new CellularAutomatonCLI() {

        @Override
        protected CellularAutomaton createCellularAutomaton() throws IOException {
          return new PrimordialCellularAutomaton(simulation);
        }

        @Override
        protected CellularAutomatonRenderer createCellularAutomatonRenderer(
            CellularAutomaton cells, int width, int height) {
          return new CellularAutomatonRenderer(cells, CELL_COLOR_PROVIDER, width, height);
        }

        @Override
        protected CellularAutomatonVisualizer createVisualizer(
            int width, int height, int framesPerSecond, int frameCount) throws IOException {
          return new CellularAutomatonVisualizer(createCellularAutomaton(), CELL_COLOR_PROVIDER,
              width, height, framesPerSecond, frameCount);
        }
      };
      CellularAutomatonCLI.main(args, cli);
    }
  }
}
