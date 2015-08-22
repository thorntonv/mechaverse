package org.mechaverse.simulation.common.cellautomaton.analysis;

import com.google.common.math.IntMath;

/**
 * CLI for analyzing boolean function representations.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class BooleanFunctionAnalysisCLI {

  public static void main(String[] args) {
    printFunctionTable(4, 16);
  }

  public static void printFunctionTable(int numVariables, int numParams) {
    int idx = 0;
    for (int row : generateFunctionTable(numVariables, numParams, 0)) {
      String rowString = String.format("%" + (numVariables + numParams) + "s%d", 
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
        int rowIndex = (x << pow2numVariables) | c;
        table[rowIndex] = value & 0b1;
        value >>= 1;
      }
    }
    return table;
  }
}
