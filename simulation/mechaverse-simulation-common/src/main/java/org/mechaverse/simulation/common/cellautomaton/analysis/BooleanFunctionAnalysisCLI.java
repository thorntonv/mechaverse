package org.mechaverse.simulation.common.cellautomaton.analysis;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.math.IntMath;

/**
 * Analyzer for boolean function representations.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class BooleanFunctionAnalyzer {

  public static void main(String args[]) {
    // printFunctionTable(3, 8);
    printFunctionCoverage3(8);
  }

  public static void printFunctionTable(int numVariables, int numParams) {
    int idx = 0;
    for (int row : generateFunctionTable(numVariables, numParams, 0)) {
      String rowString = String.format("%11s%d", 
          Integer.toBinaryString(idx), row).replace(' ', '0');
      System.out.println(rowString);
      idx++;
    }
  }

  public static int[] generateFunctionTable(int numVariables, int numParams, int offset) {
    int pow2numVariables = IntMath.pow(2, numVariables);
    int pow2numParams = IntMath.pow(2, numParams);

    int[] table = new int[pow2numVariables * pow2numParams];

    for (int c = 0; c < pow2numParams; c++) {
      int value = (c + offset) % pow2numParams;
      for (int x = 0; x < pow2numVariables; x++) {
        String formatString = String.format("%%%ds%%%ds", numVariables, pow2numVariables);
        String row = String.format(formatString, 
            Integer.toBinaryString(x), Integer.toBinaryString(c));
        row = row.replace(' ', '0');

        int rowIndex = (x << pow2numVariables) | c;
        table[rowIndex] = value & 0b1;
        value >>= 1;
      }
    }
    return table;
  }

  public static void printFunctionCoverage3(int paramCount) {
    Map<Integer, int[]> booleanFunctionParamsMap = new TreeMap<>();
    for (int cnt = 0; cnt < IntMath.pow(2, paramCount); cnt++) {
      int value = cnt;
      int paramIdx = 0;
      int[] params = new int[paramCount];
      while (value > 0) {
        params[paramIdx] = value & 0b1;
        value >>= 1;
        paramIdx++;
      }
      int function = getBooleanFunction3(params);
      booleanFunctionParamsMap.put(function, params);
    }

    for (Entry<Integer, int[]> entry : booleanFunctionParamsMap.entrySet()) {
      System.out.printf("%d - %s%n", entry.getKey(), Arrays.toString(entry.getValue()));
    }

    System.out.println("Covered " + booleanFunctionParamsMap.size() + " functions");
  }

  public static int getBooleanFunction3(int[] params) {
    int value = 0;
    int[] input = new int[3];
    for (int i0 = 0; i0 <= 1; i0++) {
      input[0] = i0;
      for (int i1 = 0; i1 <= 1; i1++) {
        input[1] = i1;
        for (int i2 = 0; i2 <= 1; i2++) {
          input[2] = i2;
          value = (value << 1) | (getValue3(input, params) & 0b1);
        }
      }
    }

    return value;
  }

  // 8 parameters, 25 operations.
  // X1 X2 X3 A + X1 X2 X3' B + X1 X2' X3 C + X1 X2' X3' D + X1' X2 X3 E + X1' X2 X3' F
  // + X1' X2' X3 G + X1' X2' X3' H
  public static int getValue3(int[] input, int[] params) {
    int notInput0 = ~input[0];
    int notInput1 = ~input[1];
    int notInput2 = ~input[2];

    int term1 = (input[0] & input[1]) & (input[2] & params[0] | notInput2 & params[1]);
    int term2 = (input[0] & notInput1) & (input[2] & params[2] | notInput2 & params[3]);
    int term3 = (notInput0 & input[1]) & (input[2] & params[4] | notInput2 & params[5]);
    int term4 = (notInput0 & notInput1 & input[2]) & params[6];
    return (term1 | term2 | term3 | term4) ^ params[7];
  }
}
