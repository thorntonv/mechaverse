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
          int i1 = input[0], i2 = input[1], i3 = input[2];
          int p1 = params[0], p2 = params[1], p3 = params[2], p4 = params[3];
          int p5 = params[4], p6 = params[5], p7 = params[6], p8 = params[7];
          
          int iInverted = ~i3;
          int t1 = i3 & p1 | iInverted & p2;
          int t2 = i3 & p3 | iInverted & p4;
          int t3 = i3 & p5 | iInverted & p6;
          int t4 = i3 & p7 | iInverted & p8;
          
          iInverted = ~i2;
          t1 = i2 & t1 | iInverted & t2;
          t2 = i2 & t3 | iInverted & t4;
          
          return i1 & t1 | ~i1 & t2 & 0b1;
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
          
          int iInverted = ~i4;
          int t1 = i4 & p1 | iInverted & p2;
          int t2 = i4 & p3 | iInverted & p4;
          int t3 = i4 & p5 | iInverted & p6;
          int t4 = i4 & p7 | iInverted & p8;
          int t5 = i4 & p9 | iInverted & p10;
          int t6 = i4 & p11 | iInverted & p12;
          int t7 = i4 & p13 | iInverted & p14;
          int t8 = i4 & p15 | iInverted & p16;
          
          iInverted = ~i3;
          t1 = i3 & t1 | iInverted & t2;
          t2 = i3 & t3 | iInverted & t4;
          t3 = i3 & t5 | iInverted & t6;
          t4 = i3 & t7 | iInverted & t8;
          
          iInverted = ~i2;
          t1 = i2 & t1 | iInverted & t2;
          t2 = i2 & t3 | iInverted & t4;
          
          return i1 & t1 | ~i1 & t2 & 0b1;
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
