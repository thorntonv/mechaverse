package org.mechaverse.simulation.common.circuit.analysis;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.common.circuit.CircuitUtil;

/**
 * Analyzer for boolean function representations.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class BooleanFunctionAnalyzer {

  public static void main(String args[]) {
    RandomGenerator random = new Well19937c();

    Map<Integer, Integer> booleanFunctionCountMap = new TreeMap<>();

    int sampleCount = 0;
    do {
      int[] params = CircuitUtil.randomState(25, random);
      int function = getBooleanFunction(params);
      Integer count = booleanFunctionCountMap.get(function);
      if (count == null) {
        count = 0;
      }
      booleanFunctionCountMap.put(function, count + 1);
      sampleCount++;
    } while(booleanFunctionCountMap.size() < 256 && sampleCount < 1000000);

    for(Entry<Integer, Integer> entry : booleanFunctionCountMap.entrySet()) {
      System.out.printf("%d,%d%n", entry.getKey(), entry.getValue());
    }
  }

  public static int getBooleanFunction(int[] params) {
    int value = 0;
    int[] input = new int[3];
    for (int i0 = 0; i0 <= 1; i0++) {
      input[0] = i0;
      for (int i1 = 0; i1 <= 1; i1++) {
        input[1] = i1;
        for (int i2 = 0; i2 <= 1; i2++) {
          input[2] = i2;
          value = (value << 1) | (getValue(input, params) & 0b1);
        }
      }
    }

    return value;
  }

  public static int getValue(int[] input, int[] params) {
    int term1 = ((input[0] ^ params[0]) & params[1]) | ((input[1] ^ params[2]) & params[3]) | ((input[2] ^ params[4]) & params[5]);
    int term2 = ((input[0] ^ params[6]) & params[7]) | ((input[1] ^ params[8]) & params[9]) | ((input[2] ^ params[10]) & params[11]);
    int term3 = ((input[0] ^ params[12]) & params[13]) | ((input[1] ^ params[14]) & params[15]) | ((input[2] ^ params[16]) & params[17]);
    int term4 = ((input[0] ^ params[18]) & params[19]) | ((input[1] ^ params[20]) & params[21]) | ((input[2] ^ params[22]) & params[23]);
    return (term1 & term2 & term3 & term4) ^ params[24];
  }
}
