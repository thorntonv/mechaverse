package org.mechaverse.simulation.experimental;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.common.AbstractEntity;
import org.mechaverse.simulation.common.SimulationConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.genetic.selection.ElitistSelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.SelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.TournamentSelectionStrategy;
import org.mechaverse.simulation.common.simple.SimpleSimulation;
import org.mechaverse.simulation.common.simple.SimpleSimulationModel;
import org.mechaverse.simulation.common.simple.SimpleSimulationState;

/**
 * A simulation to create a cellular automaton that calculates the majority function. If a majority
 * of bits are set in the top row, then after updating the automaton a majority of bits should be
 * set in the bottom row.
 */
public class MajoritySimulation {

  private static final int NUM_ENTITIES = 1000;
  private static final int RETAIN_TOP_ENTITY_COUNT = 100;
  private static final int REMOVE_BOTTOM_ENTITY_COUNT = 100;
  private static final int MAX_ITERATIONS = Integer.MAX_VALUE;
  private static final int UPDATES_PER_ITERATION = 150;

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
    private final RandomGenerator random = new Well19937c();

    @Override
    public int[] getInput() {
      TIntIntHashMap valueCountMap = new TIntIntHashMap();
      SimulatorCellularAutomaton cellularAutomaton = getCellularAutomaton();
      for (int row = 0; row < cellularAutomaton.getHeight(); row++) {
        for (int col = 0; col < cellularAutomaton.getWidth(); col++) {
          if (row == 0) {
            int value = random.nextInt() & 1;
            valueCountMap.putIfAbsent(value, 0);
            valueCountMap.increment(value);
            cellularAutomaton.getCell(0, col).setOutput(0, value);
          } else {
            cellularAutomaton.getCell(row, col).setOutput(0, 0);
          }
        }
      }

      cellularAutomaton.updateState();

      majority = valueCountMap.get(0) > valueCountMap.get(1) ? 0 : 1;

      return super.getInput();
    }

    @Override
    public void processOutput(int[] output) {
      super.processOutput(output);
      SimulatorCellularAutomaton cellularAutomaton = getCellularAutomaton();
      cellularAutomaton.refreshOutputs();

      double matchCount = 0;
      int lastRowIdx = cellularAutomaton.getHeight() - 1;
      for(int col = 0; col < cellularAutomaton.getWidth(); col++) {
        if ((cellularAutomaton.getCell(lastRowIdx, col).getOutput(0) & 1) == majority) {
          matchCount++;
        }
      }

      fitness += matchCount == cellularAutomaton.getWidth() ? 1.0 : 0.0;
    }

    @Override
    public void setCellularAutomaton(SimulatorCellularAutomaton cellularAutomaton) {
      super.setCellularAutomaton(cellularAutomaton);
      cellularAutomaton.refresh();

      int lastRowIndex = cellularAutomaton.getHeight() - 1;
      for (int col = 0; col < cellularAutomaton.getWidth(); col++) {
        cellularAutomaton.getCell(lastRowIndex, col).addOutputToOutputMap(0);
      }
      cellularAutomaton.updateOutputMap();
    }

    public double getFitness() {
      return fitness / UPDATES_PER_ITERATION;
    }

    @Override
    public String toString() {
      SimulatorCellularAutomaton cellularAutomaton = getCellularAutomaton();

      StringBuilder builder = new StringBuilder(getId() + " majority: " + majority + "\n");
      for (int row = 0; row < cellularAutomaton.getHeight(); row++) {
        for (int col = 0; col < cellularAutomaton.getWidth(); col++) {
          builder.append(cellularAutomaton.getCell(row, col).getOutput(0) & 1).append(" ");
        }
        builder.append("\n");
      }
      return builder.toString();
    }
  }

  private static class Simulation
      extends SimpleSimulation<MajorityEntity, SimpleSimulationModel> {

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
        .setOpenCLSimulator(new CellularAutomatonSimulatorConfig.Builder()
            .setNumAutomata(NUM_ENTITIES)
            .setDescriptorResource("boolean4-5x3-noinput.xml")
            .setAutomatonOutputSize(5)
            .build())
        .setSelectionStrategy(selectionStrategy).build());

    simulation.step(MAX_ITERATIONS, Integer.MAX_VALUE);
  }
}
