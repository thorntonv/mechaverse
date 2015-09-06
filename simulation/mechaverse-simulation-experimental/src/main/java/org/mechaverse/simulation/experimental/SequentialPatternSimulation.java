package org.mechaverse.simulation.experimental;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.AbstractEntity;
import org.mechaverse.simulation.common.SimulationConfig;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticData;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticData.CellGeneticData;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.LogicalUnitBuilder;
import org.mechaverse.simulation.common.genetic.selection.ElitistSelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.SelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.TournamentSelectionStrategy;
import org.mechaverse.simulation.common.simple.SimpleSimulation;
import org.mechaverse.simulation.common.simple.SimpleSimulationModel;
import org.mechaverse.simulation.common.simple.SimpleSimulationState;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

public class SequentialPatternSimulation {

  private static final int NUM_ENTITIES = 8000;
  private static final int RETAIN_TOP_ENTITY_COUNT = 75;
  private static final int REMOVE_BOTTOM_ENTITY_COUNT = 0;
  private static final int MAX_ITERATIONS = Integer.MAX_VALUE;

  private static class SequentialPatternFitnessCalculator
      implements Function<SequentialPatternEntity, Double> {

    public static final SequentialPatternFitnessCalculator INSTANCE =
        new SequentialPatternFitnessCalculator();

    @Override
    public Double apply(SequentialPatternEntity entity) {
      CellularAutomatonGeneticData geneticData = entity.getCellularAutomatonGeneticData();
      int valueCount = geneticData.getRowCount() * geneticData.getColumnCount();

      Set<Integer> values = new HashSet<>();
      int fitness = 0;
      for (int row = 0; row < geneticData.getRowCount(); row++) {
        for (int col = 0; col < geneticData.getColumnCount(); col++) {
          CellGeneticData cellData = geneticData.getCellData(row, col);

          int value = Math.abs(cellData.getData()[0]) % valueCount;
          if (!values.contains(value)) {
            values.add(value);
            fitness += getDistance(value, row, col, geneticData);
          } else {
            fitness += (geneticData.getRowCount() + geneticData.getColumnCount());
          }
        }
      }
      return (double) fitness;
    }

    protected int getDistance(int value, int row, int column,
        CellularAutomatonGeneticData geneticData) {
      int targetRow = value / geneticData.getColumnCount();
      int targetCol = value % geneticData.getColumnCount();
      return Math.abs(targetRow - row) + Math.abs(targetCol - column);
    }
  }

  private static class SequentialPatternEntitySupplier
      implements Supplier<SequentialPatternEntity> {

    @Override
    public SequentialPatternEntity get() {
      return new SequentialPatternEntity();
    }
  }

  public static class SequentialPatternEntity extends AbstractEntity {

    @Override
    protected String cellValueToString(int value) {
      CellularAutomatonGeneticData geneticData = getCellularAutomatonGeneticData();
      int valueCount = geneticData.getRowCount() * geneticData.getColumnCount();
      return String.valueOf(Math.abs(value) % valueCount);
    }
  }

  private static class Simulation
      extends SimpleSimulation<SequentialPatternEntity, SimpleSimulationModel> {

    public Simulation(SimulationConfig<SequentialPatternEntity, SimpleSimulationModel> config) {
      super(new SimpleSimulationState<>(new SimpleSimulationModel(),
          SimpleSimulationModel.SERIALIZER), config);
    }
  }

  public static void main(String[] args) throws Exception {
    SimulationConfig.Builder<SequentialPatternEntity, SimpleSimulationModel> configBuilder =
        new SimulationConfig.Builder<>();
    SelectionStrategy<SequentialPatternEntity> selectionStrategy =
        new ElitistSelectionStrategy<>(RETAIN_TOP_ENTITY_COUNT, REMOVE_BOTTOM_ENTITY_COUNT,
            new TournamentSelectionStrategy<SequentialPatternEntity>());

    Simulation simulation = new Simulation(configBuilder
        .setEntitySupplier(new SequentialPatternEntitySupplier())
        .setEntityFitnessFunction(SequentialPatternFitnessCalculator.INSTANCE)
        .setUpdatesPerIteration(0)
        .setMinimize(true)
        .setOpenCLSimulator(new CellularAutomatonSimulatorConfig.Builder()
            .setNumAutomata(NUM_ENTITIES)
            .setDescriptor(getDescriptor())
            .build())
        .setSelectionStrategy(selectionStrategy).build());

    simulation.step(MAX_ITERATIONS, 0.0);
  }

  private static CellularAutomatonDescriptor getDescriptor() throws IOException {
    return CellularAutomatonDescriptorBuilder.newBuilderFromResource("boolean4.xml")
        .setWidth(1)
        .setHeight(1)
        .setIterationsPerUpdate(1)
        .setLogicalUnit(new LogicalUnitBuilder()
            .setWidth(4)
            .setHeight(4)
            .setNeighborConnections(4)
            .setDefaultCellType("boolean4")
            .build())
        .build();
  }
}
