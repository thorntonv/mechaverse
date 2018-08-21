package org.mechaverse.simulation.common.cellautomaton.simulation;

import java.util.Random;

/**
 * A {@link CellularAutomatonSimulator} implementation that returns random output.
 */
public class RandomCellularAutomatonSimulator implements CellularAutomatonSimulator {

  private static final int RANDOM_OUTPUT_COUNT = 4096;
  private final int size;
  private final int inputSize;
  private final int stateSize;
  private final int outputSize;
  private CellularAutomatonAllocator allocator;
  private int[][] randomOutputs;
  private Random random = new Random();

  public RandomCellularAutomatonSimulator(int size, int inputSize, int stateSize, int outputSize) {
    this.size = size;
    this.inputSize = inputSize;
    this.stateSize = stateSize;
    this.outputSize = outputSize;

    this.allocator = new CellularAutomatonAllocator(size);

    randomOutputs = generateRandomOutputData(RANDOM_OUTPUT_COUNT, outputSize);
  }

  private int[][] generateRandomOutputData(int count, int size) {
    int[][] data = new int[count][size];
    for (int i = 0; i < data.length; i++) {
      data[i] = new int[size];
      for (int j = 0; j < data[i].length; j++) {
        data[i][j] = random.nextInt();
      }
    }
    return data;
  }

  @Override
  public CellularAutomatonAllocator getAllocator() {
    return allocator;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public int getAutomatonInputSize() {
    return inputSize;
  }

  @Override
  public int getAutomatonStateSize() {
    return stateSize;
  }

  @Override
  public int getAutomatonOutputSize() {
    return outputSize;
  }

  @Override
  public void getAutomatonState(int index, int[] state) {}

  @Override
  public void setAutomatonState(int index, int[] state) {}

  @Override
  public void setAutomatonInputMap(int index, int[] inputMap) {}

  @Override
  public void setAutomatonInput(int index, int[] input) {}

  @Override
  public void setAutomatonOutputMap(int index, int[] outputMap) {}

  @Override
  public void getAutomatonOutput(int index, int[] output) {
    int[] randomOutput = randomOutputs[random.nextInt(randomOutputs.length)];
    System.arraycopy(randomOutput, 0, output, 0, randomOutput.length);
  }

  @Override
  public void update() {}

  @Override
  public void close() {}
}
