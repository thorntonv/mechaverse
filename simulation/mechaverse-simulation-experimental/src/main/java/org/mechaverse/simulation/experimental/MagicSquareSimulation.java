package org.mechaverse.simulation.experimental;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticData;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticData.CellGeneticData;
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

/**
 * A simulation to find magic squares.
 *
 * https://en.wikipedia.org/wiki/Magic_square
 */
public class MagicSquareSimulation {

  private static final int NUM_ENTITIES = 10000;
  private static final int RETAIN_TOP_ENTITY_COUNT = 200;
  private static final int REMOVE_BOTTOM_ENTITY_COUNT = 0;
  private static final int MAX_ITERATIONS = Integer.MAX_VALUE;

  public static class MagicSquareFitnessCalculator implements Function<MagicSquareEntity, Double> {

    public static final MagicSquareFitnessCalculator INSTANCE = new MagicSquareFitnessCalculator();

    @Override
    public Double apply(MagicSquareEntity entity) {
      CellularAutomatonGeneticData geneticData = entity.getCellularAutomatonGeneticData();
      int valueCount = geneticData.getRowCount() * geneticData.getColumnCount();

      int fitness = 0;

      int n = geneticData.getRowCount();
      int targetSum = n * (n * n + 1) / 2;

      Set<Integer> values = new HashSet<>();
      for (int row = 0; row < geneticData.getRowCount(); row++) {
        for (int col = 0; col < geneticData.getColumnCount(); col++) {
          CellGeneticData cellData = geneticData.getCellData(row, col);
          int value = normalizeValue(cellData.getData()[0], valueCount);
          if (!values.contains(value)) {
            values.add(value);
          } else {
            cellData.getData()[0] = 0;
          }
        }
      }

      int diagonalSum1 = 0;
      int diagonalSum2 = 0;
      for (int row = 0; row < geneticData.getRowCount(); row++) {
        int rowSum = 0;
        int colSum = 0;
        for (int col = 0; col < geneticData.getColumnCount(); col++) {
          int value = normalizeValue(geneticData.getCellData(row, col).getData()[0], valueCount);
          rowSum += value;
          if (row == col) {
            diagonalSum1 += value;
          }
          if (row == (n - 1) - col) {
            diagonalSum2 += value;
          }

          value = normalizeValue(geneticData.getCellData(col, row).getData()[0], valueCount);
          colSum += value;
        }

        fitness += Math.abs(rowSum - targetSum);
        fitness += Math.abs(colSum - targetSum);
      }

      fitness += Math.abs(diagonalSum1 - targetSum);
      fitness += Math.abs(diagonalSum2 - targetSum);

      return (double) fitness;
    }

    public static int normalizeValue(int value, int valueCount) {
      return Math.abs(value) % valueCount + 1;
    }
  }

  private static class MagicSquareEntitySupplier implements Supplier<MagicSquareEntity> {

    @Override
    public MagicSquareEntity get() {
      return new MagicSquareEntity();
    }
  }

  public static class MagicSquareEntity extends SimpleCellularAutomatonEntity {

    @Override
    protected String cellValueToString(int value) {
      CellularAutomatonGeneticData geneticData = getCellularAutomatonGeneticData();
      int valueCount = geneticData.getRowCount() * geneticData.getColumnCount();
      return String.valueOf(Math.abs(value) % valueCount + 1);
    }
  }

  private static class Simulation
      extends SimpleSimulation<MagicSquareEntity> {

    public Simulation(SimpleSimulationConfig<MagicSquareEntity, SimulationModel> config) {
      super(new SimulationModel(), config);
    }
  }

  public static void main(String[] args) throws Exception {
    SimpleSimulationConfig.Builder<MagicSquareEntity, SimulationModel> configBuilder =
        new SimpleSimulationConfig.Builder<>();
    SelectionStrategy<MagicSquareEntity> selectionStrategy =
        new ElitistSelectionStrategy<>(RETAIN_TOP_ENTITY_COUNT, REMOVE_BOTTOM_ENTITY_COUNT,
            new TournamentSelectionStrategy<>());

    Simulation simulation = new Simulation(configBuilder
        .setEntitySupplier(new MagicSquareEntitySupplier())
        .setEntityFitnessFunction(MagicSquareFitnessCalculator.INSTANCE)
        .setUpdatesPerIteration(0)
        .setMinimize(true)
        .setOpenCLSimulator(new CellularAutomatonSimulatorConfig.Builder()
            .setNumAutomata(NUM_ENTITIES)
            .setDescriptor(getDescriptor())
            .build())
        .setSelectionStrategy(selectionStrategy)
        .build());

    simulation.step(MAX_ITERATIONS, 0.0);
  }

  private static CellularAutomatonDescriptor getDescriptor() throws IOException {
    return CellularAutomatonDescriptorBuilder.newBuilderFromResource("boolean4.xml")
        .setWidth(1)
        .setHeight(1)
        .setIterationsPerUpdate(1)
        .setLogicalUnit(new LogicalUnitBuilder()
            .setWidth(3)
            .setHeight(3)
            .setNeighborConnections(4)
            .setDefaultCellType("boolean4")
            .build())
        .build();
  }
}
