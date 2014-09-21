package org.mechaverse.simulation.common.util;

import java.nio.ByteBuffer;

/**
 * Utility methods for working with arrays.
 */
public class ArrayUtil {

  /**
   * Returns the given byte array as an integer array.
   */
  public static int[] toIntArray(byte[] byteArray) {
    return ByteBuffer.wrap(byteArray).asIntBuffer().array();
  }

  /**
   * Returns the given integer array as a byte array.
   */
  public static byte[] toByteArray(int[] intArray) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(intArray.length * 4);
    byteBuffer.asIntBuffer().put(intArray);
    return byteBuffer.array();
  }
}
