package org.mechaverse.simulation.common.circuit;

/**
 * Circuit utility methods.
 */
public class CircuitUtil {

  public static final int BYTES_PER_INT = Integer.SIZE / Byte.SIZE;

  public static int stateSizeInBytes(int size) {
    return size * BYTES_PER_INT;
  }

  public static int outputSizeInBytes(int size) {
    return size * BYTES_PER_INT;
  }
}
