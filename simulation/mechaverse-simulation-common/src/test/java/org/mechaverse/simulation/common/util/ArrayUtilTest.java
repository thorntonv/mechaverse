package org.mechaverse.simulation.common.util;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Unit test for {@link ArrayUtil}.
 */
public class ArrayUtilTest {

  @Test
  public void testFromAndToIntArray() {
    int[] expectedIntArray = {2, 4, 6, 8};
    byte[] byteArray = ArrayUtil.toByteArray(expectedIntArray);
    int[] intArray = ArrayUtil.toIntArray(byteArray);
    assertArrayEquals(expectedIntArray, intArray);
  }
}
