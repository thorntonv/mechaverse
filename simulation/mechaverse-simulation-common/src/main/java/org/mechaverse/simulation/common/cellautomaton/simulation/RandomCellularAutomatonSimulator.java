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
  private Random random = new Random();

  public RandomCellularAutomatonSimulator(int size, int inputSize, int stateSize, int outputSize) {
    this.size = size;
    this.inputSize = inputSize;
    this.stateSize = stateSize;
    this.outputSize = outputSize;

    this.allocator = new CellularAutomatonAllocator(size);
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
    for(int idx = 0; idx < output.length; idx++) {
      output[idx] = random.nextInt();
    }
  }

  @Override
  public void update() {}

  @Override
  public void close() {}
}
