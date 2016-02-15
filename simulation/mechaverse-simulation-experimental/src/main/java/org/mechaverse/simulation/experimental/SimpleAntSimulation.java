package org.mechaverse.simulation.experimental;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.AbstractEntity;
import org.mechaverse.simulation.common.SimulationConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.genetic.selection.ElitistSelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.SelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.TournamentSelectionStrategy;
import org.mechaverse.simulation.common.model.TurnDirection;
import org.mechaverse.simulation.common.simple.SimpleSimulation;
import org.mechaverse.simulation.common.simple.SimpleSimulationModel;
import org.mechaverse.simulation.common.simple.SimpleSimulationState;
import org.mechaverse.simulation.common.util.RandomUtil;

import java.io.IOException;

/**
 * A simulation to determine if ants can learn to turn in the direction of food.
 */
public class SimpleAntSimulation {

  private static final int NUM_ENTITIES = 1000;
  private static final int RETAIN_TOP_ENTITY_COUNT = 50;
  private static final int REMOVE_BOTTOM_ENTITY_COUNT = 50;
  private static final int MAX_ITERATIONS = Integer.MAX_VALUE;
  private static final int UPDATES_PER_ITERATION = 100;


  public static class AntFitnessCalculator implements Function<AntEntity, Double> {

    public static final AntFitnessCalculator INSTANCE = new AntFitnessCalculator();

    @Override
    public Double apply(AntEntity entity) {
      return entity.getFitness();
    }
  }

  private static class AntSupplier implements Supplier<AntEntity> {

    @Override
    public AntEntity get() {
      return new AntEntity();
    }
  }

  private static class AntEntity extends AbstractEntity {

    private static final class Input {

      private static final int DATA_SIZE = 3;

      private int[] data = new int[3];

      public void setFrontLeftFood(boolean present) {
        data[0] = present ? 1 : 0;
      }

      public void setFrontFood(boolean present) {
        data[1] = present ? 1 : 0;
      }

      public void setFrontRightFood(boolean present) {
        data[2] = present ? 1 : 0;
      }

      public int[] getData() {
        return data;
      }
    }

    private static final class Output {

      private static final int DATA_SIZE = 2;
      private TurnDirection direction = TurnDirection.NONE;

      public void setData(int[] data) {
        boolean counterClockwise = (data[0] & 0b1) == 1;
        boolean clockwise = (data[1] & 0b1) == 1;

        if (counterClockwise && !clockwise) {
          direction = TurnDirection.CLOCKWISE;
        } else if (!counterClockwise && clockwise) {
          direction = TurnDirection.COUNTERCLOCKWISE;
        } else {
          direction = TurnDirection.NONE;
        }
      }

      public TurnDirection getTurnDirection() {
        return direction;
      }
    }

    private static final TurnDirection[] TURN_DIRECTIONS = TurnDirection.values();

    private final Input antInput = new Input();
    private final Output antOutput = new Output();
    private final RandomGenerator random = RandomUtil.newGenerator();

    private double fitness = 0.0;
    private TurnDirection expectedTurnDirection = TurnDirection.CLOCKWISE;

    public int[] getInput() {
      expectedTurnDirection = TURN_DIRECTIONS[random.nextInt(TURN_DIRECTIONS.length)];

      // Ant is facing north.
      switch (expectedTurnDirection) {
        case CLOCKWISE:
          antInput.setFrontLeftFood(false);
          antInput.setFrontRightFood(true);
          antInput.setFrontFood(false);
          break;
        case COUNTERCLOCKWISE:
          antInput.setFrontLeftFood(true);
          antInput.setFrontRightFood(false);
          antInput.setFrontFood(false);
          break;
        case NONE:
          antInput.setFrontLeftFood(false);
          antInput.setFrontRightFood(false);
          antInput.setFrontFood(true);
          break;
      }

      return antInput.getData();
    }

    public void processOutput(int[] output) {
      antOutput.setData(output);

      if (antOutput.getTurnDirection() == expectedTurnDirection) {
        fitness += 1;
      }
    }

    public double getFitness() {
      return fitness / UPDATES_PER_ITERATION;
    }

    @Override
    public void setCellularAutomaton(SimulatorCellularAutomaton cellularAutomaton) {
      super.setCellularAutomaton(cellularAutomaton);

      cellularAutomaton.getCell(0, 0).addOutputToInputMap(0);
      cellularAutomaton.getCell(0, cellularAutomaton.getWidth() / 2).addOutputToInputMap(0);
      cellularAutomaton.getCell(0, cellularAutomaton.getWidth() - 1).addOutputToInputMap(0);

      cellularAutomaton.getCell(cellularAutomaton.getHeight() - 1, 0)
          .addOutputToOutputMap(0);
      cellularAutomaton.getCell(cellularAutomaton.getHeight() - 1, cellularAutomaton.getWidth() - 1)
          .addOutputToOutputMap(0);

      cellularAutomaton.updateInputMap();
      cellularAutomaton.updateOutputMap();
    }

    public String toString() {
      return getId();
    }
  }

  private static class Simulation extends SimpleSimulation<AntEntity, SimpleSimulationModel> {

    public Simulation(SimulationConfig<AntEntity, SimpleSimulationModel> config) {
      super(new SimpleSimulationState<>(new SimpleSimulationModel(),
          SimpleSimulationModel.SERIALIZER), config);
    }
  }

  public static void main(String[] args) throws Exception {
    SimulationConfig.Builder<AntEntity, SimpleSimulationModel> configBuilder =
        new SimulationConfig.Builder<>();
    SelectionStrategy<AntEntity> selectionStrategy = new ElitistSelectionStrategy<>(
        RETAIN_TOP_ENTITY_COUNT, REMOVE_BOTTOM_ENTITY_COUNT,
            new TournamentSelectionStrategy<AntEntity>());
    //selectionStrategy = new NoSelectionStrategy<>();

    Simulation simulation = new Simulation(configBuilder
        .setEntitySupplier(new AntSupplier())
        .setEntityFitnessFunction(AntFitnessCalculator.INSTANCE)
        .setUpdatesPerIteration(UPDATES_PER_ITERATION)
        .setOpenCLSimulator(new CellularAutomatonSimulatorConfig.Builder()
            .setNumAutomata(NUM_ENTITIES)
            .setDescriptor(getDescriptor())
            .setAutomatonInputSize(AntEntity.Input.DATA_SIZE)
            .setAutomatonOutputSize(AntEntity.Output.DATA_SIZE)
            .build())
        .setSelectionStrategy(selectionStrategy)
        .build());

    simulation.step(MAX_ITERATIONS, 1.0);
  }

  public static CellularAutomatonDescriptor getDescriptor() throws IOException {
    return CellularAutomatonDescriptorReader.read(
        ClassLoader.getSystemResourceAsStream("boolean4.xml"));
  }
}
