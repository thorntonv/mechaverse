package org.mechaverse.simulation.experimental;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.AbstractEntity;
import org.mechaverse.simulation.common.SimulationConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.LogicalUnitBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.genetic.selection.ElitistSelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.SelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.TournamentSelectionStrategy;
import org.mechaverse.simulation.common.simple.SimpleSimulation;
import org.mechaverse.simulation.common.simple.SimpleSimulationModel;
import org.mechaverse.simulation.common.simple.SimpleSimulationState;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A simulation to create a cellular automaton that calculates the majority function. If a majority
 * of bits are set in the top row, then after updating the automaton all bits in the bottom row
 * should be set to the majority value.
 */
public class MajoritySimulation {

  private static final int NUM_ENTITIES = 2500;
  private static final int RETAIN_TOP_ENTITY_COUNT = 125;
  private static final int REMOVE_BOTTOM_ENTITY_COUNT = 125;
  private static final int MAX_ITERATIONS = Integer.MAX_VALUE;
  private static final int WIDTH = 5;
  private static final int HEIGHT = 3;
  private static final int UPDATES_PER_ITERATION = 32;

  public static class MajorityFitnessCalculator implements Function<MajorityEntity, Double> {

    public static final MajorityFitnessCalculator INSTANCE = new MajorityFitnessCalculator();

    @Override
    public Double apply(MajorityEntity entity) {
      return entity.getFitness();
    }
  }

  private static class MagicSquareEntitySupplier implements Supplier<MajorityEntity> {

    @Override
    public MajorityEntity get() {
      return new MajorityEntity();
    }
  }

  public static class MajorityEntity extends AbstractEntity {

    private double fitness = 0.0;
    private int majority;
    private int[] input;
    private int nextValue = 0;

    @Override
    public int[] getInput() {
      for (int idx = 0; idx < input.length; idx++) {
        input[idx] = 0;
      }

      int zeroCount = 0;
      int oneCount = 0;
      int value = nextValue++;
      for (int idx = 0; idx < getCellularAutomaton().getWidth(); idx++) {
        input[idx] = value & 1;
        value >>= 1;
        if (input[idx] == 0) {
          zeroCount++;
        } else {
          oneCount++;
        }
      }

      majority = zeroCount > oneCount ? 0 : 1;

      return input;
    }

    @Override
    public void processOutput(int[] output) {
      super.processOutput(output);

      int matchCount = 0;
      for (int outputValue : output) {
        if ((outputValue & 1) == majority) {
          matchCount++;
        }
      }

      fitness += matchCount == output.length ? 1.0 : 0.0;
    }

    @Override
    public void setCellularAutomaton(SimulatorCellularAutomaton cellularAutomaton) {
      super.setCellularAutomaton(cellularAutomaton);

      this.input = new int[cellularAutomaton.getSimulator().getAutomatonInputSize()];

      for (int row = 0; row < cellularAutomaton.getHeight(); row++) {
        for (int col = 0; col < cellularAutomaton.getWidth(); col++) {
          cellularAutomaton.getCell(row, col).addOutputToInputMap(0);
        }
      }

      int lastRowIndex = cellularAutomaton.getHeight() - 1;
      for (int col = 0; col < cellularAutomaton.getWidth(); col++) {
        cellularAutomaton.getCell(lastRowIndex, col).addOutputToOutputMap(0);
      }
      cellularAutomaton.updateInputMap();
      cellularAutomaton.updateOutputMap();
    }

    public double getFitness() {
      return fitness / UPDATES_PER_ITERATION;
    }

    @Override
    public String toString() {
      SimulatorCellularAutomaton cellularAutomaton = getCellularAutomaton();
      cellularAutomaton.refresh();
      StringBuilder builder = new StringBuilder(getId() + " majority: " + majority + "\n");
      for (int row = 0; row < cellularAutomaton.getHeight(); row++) {
        for (int col = 0; col < cellularAutomaton.getWidth(); col++) {
          SimulatorCellularAutomaton.SimulatorCellularAutomatonCell cell =
              cellularAutomaton.getCell(row, col);
          int value = cell.getOutput(0) & 1;
          if (row == 0) {
            value = input[col] & 1;
          }
          builder.append(value).append(" ");
        }
        builder.append("\n");
      }
      return builder.toString();
    }
  }

  private static class Simulation extends SimpleSimulation<MajorityEntity, SimpleSimulationModel> {

    public Simulation(SimulationConfig<MajorityEntity, SimpleSimulationModel> config) {
      super(new SimpleSimulationState<>(new SimpleSimulationModel(),
          SimpleSimulationModel.SERIALIZER), config);
    }
  }

  public static void main(String[] args) throws Exception {
    SimulationConfig.Builder<MajorityEntity, SimpleSimulationModel> configBuilder =
        new SimulationConfig.Builder<>();
    SelectionStrategy<MajorityEntity> selectionStrategy =
        new ElitistSelectionStrategy<>(RETAIN_TOP_ENTITY_COUNT, REMOVE_BOTTOM_ENTITY_COUNT,
            new TournamentSelectionStrategy<MajorityEntity>());
    // selectionStrategy = new NoSelectionStrategy<>();

    Simulation simulation = new Simulation(configBuilder
        .setEntitySupplier(new MagicSquareEntitySupplier())
        .setEntityFitnessFunction(MajorityFitnessCalculator.INSTANCE)
        .setUpdatesPerIteration(UPDATES_PER_ITERATION)
        .setOpenCLSimulator(new CellularAutomatonSimulatorConfig.Builder().setNumAutomata(NUM_ENTITIES)
            .setDescriptor(getDescriptor())
            .setAutomatonInputSize(WIDTH * HEIGHT)
            .setAutomatonOutputSize(WIDTH)
            .build())
        .setSelectionStrategy(selectionStrategy)
        .build());

    simulation.step(MAX_ITERATIONS, 1.0);

    MajorityEntity bestEntity = simulation.getLogger().getOverallBestEntity().getFirst();
    try(OutputStream out = new FileOutputStream("entity.out")) {
      out.write(bestEntity.getCellularAutomatonGeneticData().getData());
    }
  }

  public static final CellularAutomatonDescriptor getDescriptor() throws IOException {
    return CellularAutomatonDescriptorBuilder.newBuilderFromResource("boolean4.xml")
        .setWidth(1)
        .setHeight(1)
        .setIterationsPerUpdate(50)
        .setLogicalUnit(new LogicalUnitBuilder()
            .setWidth(WIDTH)
            .setHeight(HEIGHT)
            .setNeighborConnections(4)
            .setDefaultCellType("boolean4")
            .build())
        .build();
  }
}
