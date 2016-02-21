package org.mechaverse.simulation.experimental.ant;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.AbstractEntity;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.model.TurnDirection;
import org.mechaverse.simulation.common.util.RandomUtil;

public class AntEntity extends AbstractEntity {

  public static final class Input {

    public static final int DATA_SIZE = 3;

    private int[] data = new int[DATA_SIZE];

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

  public static final class Output {

    public static final int DATA_SIZE = 3;
    private TurnDirection direction = TurnDirection.NONE;

    public void setData(int[] data) {
      boolean counterClockwise = (data[0] & 0b1) == 1;
      boolean consume = (data[1] & 0b1) == 1;
      boolean clockwise = (data[2] & 0b1) == 1;

      direction = null;
      if (counterClockwise && !clockwise) {
        direction = TurnDirection.COUNTERCLOCKWISE;
      } else if (!counterClockwise && clockwise) {
        direction = TurnDirection.CLOCKWISE;
      } else if (consume) {
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
  private int updates = 0;
  private TurnDirection expectedTurnDirection = TurnDirection.CLOCKWISE;

  public static void initCellularAutomaton(final SimulatorCellularAutomaton cellularAutomaton) {
    cellularAutomaton.getCell(0, 0).addOutputToInputMap(0);
    cellularAutomaton.getCell(0, cellularAutomaton.getWidth() / 2).addOutputToInputMap(0);
    cellularAutomaton.getCell(0, cellularAutomaton.getWidth() - 1).addOutputToInputMap(0);

    cellularAutomaton.getCell(cellularAutomaton.getHeight() / 2, 0).addOutputToOutputMap(0);
    cellularAutomaton.getCell(cellularAutomaton.getHeight() / 2, cellularAutomaton.getWidth() / 2)
        .addOutputToOutputMap(0);
    cellularAutomaton.getCell(cellularAutomaton.getHeight() / 2, cellularAutomaton.getWidth() - 1)
        .addOutputToOutputMap(0);

    cellularAutomaton.updateInputMap();
    cellularAutomaton.updateOutputMap();
  }

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
    updates++;
  }

  public double getFitness() {
    return fitness / updates;
  }

  public void setFitness(int fitness) {
    this.fitness = fitness;
  }

  @Override
  public void setCellularAutomaton(SimulatorCellularAutomaton cellularAutomaton) {
    super.setCellularAutomaton(cellularAutomaton);
    initCellularAutomaton(cellularAutomaton);
  }

  public String toString() {
    return getId();
  }
}
