package org.mechaverse.simulation.experimental;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticData;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.LogicalUnitBuilder;
import org.mechaverse.simulation.common.genetic.selection.ElitistSelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.SelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.TournamentSelectionStrategy;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.experimental.simple.SimpleCellularAutomatonEntity;
import org.mechaverse.simulation.experimental.simple.SimpleSimulation;
import org.mechaverse.simulation.experimental.simple.SimpleSimulationConfig;

public class CheckerboardPatternSimulation {

  private static final int NUM_ENTITIES = 500;
  private static final int RETAIN_TOP_ENTITY_COUNT = 50;
  private static final int REMOVE_BOTTOM_ENTITY_COUNT = 0;
  private static final int MAX_ITERATIONS = Integer.MAX_VALUE;

  private static class CheckerboardPatternEntitySupplier
      implements Supplier<CheckerboardPatternEntity> {

    @Override
    public CheckerboardPatternEntity get() {
      return new CheckerboardPatternEntity();
    }
  }

  public static class CheckerboardPatternEntity extends SimpleCellularAutomatonEntity {

    @Override
    protected String cellValueToString(int value) {
      return String.valueOf(value % 2);
    }
  }

  public static class CheckerboardPatternFitnessFunction
      implements Function<CheckerboardPatternEntity, Double> {

    public static final CheckerboardPatternFitnessFunction INSTANCE =
        new CheckerboardPatternFitnessFunction();

    @Override
    public Double apply(CheckerboardPatternEntity entity) {
      int fitness = 0;
      CellularAutomatonGeneticData geneticData = entity.getCellularAutomatonGeneticData();
      for (int row = 0; row < geneticData.getRowCount(); row++) {
        for (int col = 0; col < geneticData.getColumnCount(); col++) {
          int value = geneticData.getCellData(row, col).getData()[0];
          int targetValue = (row % 2 == 0) ? col % 2 : (col + 1) % 2;
          fitness += value > 0 && value % 2 == targetValue ? 1 : 0;
        }
      }
      return (double) fitness;
    }
  }

  private static class Simulation
      extends SimpleSimulation<CheckerboardPatternEntity> {

    public Simulation(SimpleSimulationConfig<CheckerboardPatternEntity, SimulationModel> config) {
        super(new SimulationModel(), config);
    }
  }

  public static void main(String[] args) throws Exception {
    SimpleSimulationConfig.Builder<CheckerboardPatternEntity, SimulationModel> configBuilder =
        new SimpleSimulationConfig.Builder<>();
    SelectionStrategy<CheckerboardPatternEntity> selectionStrategy = new ElitistSelectionStrategy<>(
        RETAIN_TOP_ENTITY_COUNT, REMOVE_BOTTOM_ENTITY_COUNT,
            new TournamentSelectionStrategy<>());

    Simulation simulation = new Simulation(configBuilder
        .setEntitySupplier(new CheckerboardPatternEntitySupplier())
        .setEntityFitnessFunction(CheckerboardPatternFitnessFunction.INSTANCE)
        .setUpdatesPerIteration(0)
        .setOpenCLSimulator(new CellularAutomatonSimulatorConfig.Builder()
            .setNumAutomata(NUM_ENTITIES)
            .setDescriptor(getDescriptor())
            .build())
        .setSelectionStrategy(selectionStrategy)
        .build());

    simulation.step(MAX_ITERATIONS, 64);
  }

  private static CellularAutomatonDescriptor getDescriptor() throws IOException {
    return CellularAutomatonDescriptorBuilder.newBuilderFromResource("boolean4.xml")
        .setWidth(2)
        .setHeight(2)
        .setIterationsPerUpdate(1)
        .setLogicalUnit(new LogicalUnitBuilder()
            .setWidth(4)
            .setHeight(4)
            .setNeighborConnections(4)
            .setDefaultCellType(CellularAutomatonDescriptorBuilder.BOOLEAN_4INPUT_CELL_TYPE)
            .build())
        .build();
  }
}
