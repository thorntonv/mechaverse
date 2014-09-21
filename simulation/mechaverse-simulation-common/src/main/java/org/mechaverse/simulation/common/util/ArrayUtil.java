package org.mechaverse.simulation.common.util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Utility methods for working with arrays.
 */
public class ArrayUtil {

  /**
   * Returns a new integer array using data from the given byte array.
   */
  public static int[] toIntArray(byte[] byteArray) {
    IntBuffer intBuffer = ByteBuffer.wrap(byteArray).asIntBuffer();
    int[] intArray = new int[intBuffer.limit()];
    intBuffer.get(intArray);
    return intArray;
  }

  /**
   * Returns a new byte array using data from the given integer array.
   */
  public static byte[] toByteArray(int[] intArray) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(intArray.length * 4);
    byteBuffer.asIntBuffer().put(intArray);
    return byteBuffer.array();
  }
}
