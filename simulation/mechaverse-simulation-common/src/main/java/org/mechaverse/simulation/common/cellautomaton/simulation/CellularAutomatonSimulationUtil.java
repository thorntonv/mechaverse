package org.mechaverse.simulation.common.cellautomaton.simulation;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Circuit utility methods.
 */
public class CellularAutomatonSimulationUtil {

  public static final int BYTES_PER_INT = Integer.SIZE / Byte.SIZE;

  /**
   * Returns a random cellular automaton state with the given length.
   */
  public static int[] randomState(int length, RandomGenerator random) {
    int[] state = new int[length];
    for (int idx = 0; idx < state.length; idx++) {
      state[idx] = random.nextInt();
    }
    return state;
  }

  public static int stateSizeInBytes(int size) {
    return size * BYTES_PER_INT;
  }

  public static int outputSizeInBytes(int size) {
    return size * BYTES_PER_INT;
  }
}
