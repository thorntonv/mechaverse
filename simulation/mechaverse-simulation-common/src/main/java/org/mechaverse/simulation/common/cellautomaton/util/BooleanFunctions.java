package org.mechaverse.simulation.common.cellautomaton.util;

import java.util.Map;
import java.util.TreeMap;

import com.google.common.math.IntMath;

/**
 * Boolean function utility methods.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class BooleanFunctions {

  public static interface ParameterizedBooleanFunction {

    int getValue(int[] input, int[] params);
  }

  /**
   * X1  X2 X3 A + X1  X2 X3' B + X1  X2' X3 C + X1  X2' X3' D + 
   * X1' X2 X3 E + X1' X2 X3' F + X1' X2' X3 G + X1' X2' X3' H
   */
  public static final ParameterizedBooleanFunction BOOLEAN_FUCNTION_3INPUT =
      new ParameterizedBooleanFunction() {
        @Override
        public int getValue(int[] input, int[] params) {
          int notInput0 = ~input[0];
          int notInput1 = ~input[1];
          int notInput2 = ~input[2];

          int term1 = (input[0] & input[1]) & (input[2] & params[0] | notInput2 & params[1]);
          int term2 = (input[0] & notInput1) & (input[2] & params[2] | notInput2 & params[3]);
          int term3 = (notInput0 & input[1]) & (input[2] & params[4] | notInput2 & params[5]);
          int term4 = (notInput0 & notInput1 & input[2]) & params[6];
          return (term1 | term2 | term3 | term4) ^ params[7];
        }
      };

  public static final ParameterizedBooleanFunction BOOLEAN_FUCNTION_4INPUT =
      new ParameterizedBooleanFunction() {
        @Override
        public int getValue(int[] input, int[] params) {
          int i1 = input[0], i2 = input[1], i3 = input[2], i4 = input[3];
          int p1 = params[0], p2 = params[1], p3 = params[2], p4 = params[3];
          int p5 = params[4], p6 = params[5], p7 = params[6], p8 = params[7];
          int p9 = params[8], p10 = params[9], p11 = params[10], p12 = params[11];
          int p13 = params[12], p14 = params[13], p15 = params[14], p16 = params[15];
          return (i1 & i2 & (i3 & i4 & p1 | i3 & ~i4 & p2 | ~i3 & i4 & p3 | ~i3 & ~i4 & p4))
              | (i1 & ~i2 & (i3 & i4 & p5 | i3 & ~i4 & p6 | ~i3 & i4 & p7 | ~i3 & ~i4 & p8))
              | (~i1 & i2 & (i3 & i4 & p9 | i3 & ~i4 & p10 | ~i3 & i4 & p11 | ~i3 & ~i4 & p12))
              | (~i1 & ~i2 & (i3 & i4 & p13 | i3 & ~i4 & p14 | ~i3 & i4 & p15 | ~i3 & ~i4 & p16))
              & 0b1;
        }
      };

  /**
   * Returns a map that maps each boolean function to the parameters for the function.
   */
  public static Map<Integer, int[]> getBooleanFunctionParamsMap(int inputCount, int paramCount,
      ParameterizedBooleanFunction booleanFunction) {
    Map<Integer, int[]> booleanFunctionParamsMap = new TreeMap<>();
    for (int cnt = 0; cnt < IntMath.pow(2, paramCount); cnt++) {
      int[] params = getBitsAsIntArray(cnt);
      int function = getBooleanFunctionValues(inputCount, booleanFunction, params);
      booleanFunctionParamsMap.put(function, params);
    }
    return booleanFunctionParamsMap;
  }

  /**
   * Returns the integer that uniquely represents the boolean function specified by the given
   * parameters.
   */
  public static int getBooleanFunctionValues(int inputCount,
      ParameterizedBooleanFunction booleanFunction, int[] params) {
    int value = 0;
    for (int inputValue = 0; inputValue < IntMath.pow(2, inputCount); inputValue++) {
      value <<= 1;
      value |= booleanFunction.getValue(getBitsAsIntArray(inputValue), params);
    }
    return value;
  }

  /**
   * Returns the bits of the given value as an integer array where the integer at index i is 1 if
   * bit i is set and 0 otherwise.
   */
  public static int[] getBitsAsIntArray(int value) {
    int[] result = new int[32];
    int idx = 0;
    while (value > 0) {
      result[idx] = value & 0b1;
      value >>= 1;
      idx++;
    }
    return result;
  }

  /**
   * Returns the value of the bits in the given array where the bit at index i is 1 if the value at
   * index i in the array is 1 and 0 otherwise.
   */
  public static int getIntFromBitIntArray(int[] value) {
    int result = 0;
    for (int idx = 0; idx < value.length; idx++) {
      result <<= 1;
      result |= (value[idx] & 0b1);
    }
    return result;
  }
}
