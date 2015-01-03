package org.mechaverse.simulation.common.cellautomaton.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

/**
 * Unit tests for {@link BooleanFunctions}.
 */
public class BooleanFunctionsTest {

  @Test
  public void testThreeInputBooleanFunctionCoverage() {
    int expectedFunctionCount = 256;
    Map<Integer, int[]> booleanFunctionParamsMap = BooleanFunctions.getBooleanFunctionParamsMap(
        3, 8, BooleanFunctions.BOOLEAN_FUCNTION_3INPUT);
    assertEquals(expectedFunctionCount, booleanFunctionParamsMap.size());
    for (int value = 0; value < expectedFunctionCount; value++) {
      assertTrue(booleanFunctionParamsMap.containsKey(value));
    }
  }
  
  @Test
  public void testFourInputBooleanFunctionCoverage() {
    int expectedFunctionCount = 65536;
    Map<Integer, int[]> booleanFunctionParamsMap = BooleanFunctions.getBooleanFunctionParamsMap(
        4, 16, BooleanFunctions.BOOLEAN_FUCNTION_4INPUT);
    assertEquals(expectedFunctionCount, booleanFunctionParamsMap.size());
    for (int value = 0; value < expectedFunctionCount; value++) {
      assertTrue(booleanFunctionParamsMap.containsKey(value));
    }
  }
  
  @Test
  public void testGetBitsAsIntArray() {
    int[] expectedArray = new int[32];
    expectedArray[2] = 1;
    expectedArray[1] = 1;
    expectedArray[0] = 0;
    assertArrayEquals(expectedArray, BooleanFunctions.getBitsAsIntArray(6));
  }
  
  @Test
  public void testGetBitsFromIntArray() {
    assertEquals(6, BooleanFunctions.getIntFromBitIntArray(new int[]{1, 1, 0}));
  }
}
