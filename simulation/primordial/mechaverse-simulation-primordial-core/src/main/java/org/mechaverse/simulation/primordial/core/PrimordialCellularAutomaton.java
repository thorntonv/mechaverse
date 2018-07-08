package org.mechaverse.simulation.primordial.core;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import org.mechaverse.simulation.common.cellautomaton.examples.CellularAutomatonCLI;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonRenderer;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonVisualizer;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SuppressWarnings("WeakerAccess")
public class PrimordialCellularAutomaton implements CellularAutomaton {

  private static final class CellAdapter implements Cell {

    private org.mechaverse.simulation.primordial.core.Cell primordialCell;

    public CellAdapter(org.mechaverse.simulation.primordial.core.Cell primordialCell) {
      this.primordialCell = primordialCell;
    }

    @Override
    public int getOutput(int idx) {
      List<EntityModel> entities = primordialCell.getEntities();
      if (entities.size() < idx || entities.get(idx) == null) {
        return -1;
      }
      EntityType entityType = EntityUtil.getType(entities.get(idx));
      return entityType != null ? entityType.ordinal() : -1;
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

  private static final Function<Cell, Color> CELL_COLOR_PROVIDER = cell -> {
    if (cell.getOutput(0) == EntityType.ENTITY.ordinal()) {
      return Color.WHITE;
    }
    if (cell.getOutput(0) == EntityType.FOOD.ordinal()) {
      return Color.GREEN;
    }
    return Color.BLACK;
  };

  public static void main(String[] args) throws IOException {

    final PrimordialSimulationImpl simulation;
    try (ClassPathXmlApplicationContext context =
             new ClassPathXmlApplicationContext("simulation-context.xml")) {
      simulation = context.getBean(PrimordialSimulationImpl.class);
      simulation.setState(simulation.generateRandomState());

      CellularAutomatonCLI cli = new CellularAutomatonCLI() {

        @Override
        protected CellularAutomaton createCellularAutomaton() {
          return new PrimordialCellularAutomaton(simulation);
        }

        @Override
        protected CellularAutomatonRenderer createCellularAutomatonRenderer(
            CellularAutomaton cells, int width, int height) {
          return new CellularAutomatonRenderer(cells, CELL_COLOR_PROVIDER, width, height);
        }

        @Override
        protected CellularAutomatonVisualizer createVisualizer(
            int width, int height, int framesPerSecond, int frameCount) {
          CellularAutomatonVisualizer visualizer = new CellularAutomatonVisualizer(createCellularAutomaton(), CELL_COLOR_PROVIDER,
              width, height, framesPerSecond, frameCount);
          visualizer.start();
          return visualizer;
        }
      };
      CellularAutomatonCLI.main(args, cli);
      System.in.read();
    }
  }
}
