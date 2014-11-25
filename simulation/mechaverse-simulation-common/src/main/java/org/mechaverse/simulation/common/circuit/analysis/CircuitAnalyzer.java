package org.mechaverse.simulation.common.circuit.analysis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;

import com.google.common.base.Preconditions;

/**
 * Analyzes a circuit. Computes the cycle start and length if the circuit has a cycle. Computes the
 * average difference (in bits) between consecutive states. The difference is computed over the
 * cycle if the circuit has a cycle otherwise the difference is over the entire provided range.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CircuitAnalyzer {

  private static class CircuitStateRecord {

    private int iteration;
    private int[] circuitState;

    public CircuitStateRecord(int iteration, int[] circuitState) {
      this.iteration = iteration;
      this.circuitState = circuitState;
    }

    public int getIteration() {
      return iteration;
    }

    public int[] getCircuitState() {
      return circuitState;
    }
  }

  // A list of all provided states.
  private ArrayList<int[]> stateHistory = new ArrayList<>();
  // Maps hash codes to the states which have the hash code.
  private Map<Integer, ArrayList<CircuitStateRecord>> previousStatesMap = new HashMap<>();
  private int cycleStartIteration;
  private int cycleLength;
  private BigInteger averageStateDifference;
  private BigInteger averageSetBitCount;
  private final CircuitSimulationModel circuitModel;

  public CircuitAnalyzer(Circuit circuit) {
    circuitModel = CircuitSimulationModelBuilder.build(circuit);
  }

  public void update(int iteration, int[] circuitState) {
    if (cycleLength > 0) {
      // Return if a cycle has already been detected.
      return;
    }

    // Truncate the constant portion of the state.
    circuitState = Arrays.copyOf(circuitState, circuitModel.getElementOutputStateSize());
    stateHistory.add(circuitState);

    int hashCode = Arrays.hashCode(circuitState);
    ArrayList<CircuitStateRecord> previousStatesWithHash = previousStatesMap.get(hashCode);
    if (previousStatesWithHash == null) {
      previousStatesWithHash = new ArrayList<CircuitStateRecord>();
      previousStatesMap.put(hashCode, previousStatesWithHash);
    }

    for (CircuitStateRecord previousStateWithHash : previousStatesWithHash) {
      if (Arrays.equals(circuitState, previousStateWithHash.getCircuitState())) {
        cycleStartIteration = previousStateWithHash.getIteration();
        int cycleEndIteration = iteration;
        cycleLength = cycleEndIteration - cycleStartIteration;
        int cycleStartIdx = cycleStartIteration - 1;
        int cycleEndIdx = cycleEndIteration - 1;

        averageStateDifference = BigInteger.ZERO;
        averageSetBitCount = getSetBitCount(stateHistory.get(cycleStartIdx));
        for (int idx = cycleStartIdx + 1; idx <= cycleEndIdx; idx++) {
          averageStateDifference = averageStateDifference.add(BigInteger.valueOf(
              getDifferenceInBits(stateHistory.get(idx - 1), stateHistory.get(idx))));
          averageSetBitCount = averageSetBitCount.add(getSetBitCount(stateHistory.get(idx)));
        }
        averageStateDifference = averageStateDifference.divide(BigInteger.valueOf(cycleLength));
        averageSetBitCount = averageSetBitCount.divide(BigInteger.valueOf(cycleLength + 1));
        return;
      }
    }

    previousStatesWithHash.add(new CircuitStateRecord(iteration, circuitState));
  }

  public int getCycleStartIteration() {
    return cycleStartIteration;
  }

  public int getCycleLength() {
    return cycleLength;
  }

  public int getAverageStateDifference() {
    if (cycleLength == 0 && averageStateDifference == null) {
      // If there is no cycle return the average across the entire history.
      averageStateDifference = BigInteger.ZERO;
      for (int idx = 1; idx < stateHistory.size(); idx++) {
        averageStateDifference = averageStateDifference.add(BigInteger.valueOf(
            getDifferenceInBits(stateHistory.get(idx - 1), stateHistory.get(idx))));
      }
      averageStateDifference =
          averageStateDifference.divide(BigInteger.valueOf(stateHistory.size()));
    }
    return averageStateDifference.intValue();
  }

  public int getAverageSetBitCount() {
    if (cycleLength == 0 && averageSetBitCount == null) {
      // If there is no cycle return the average across the entire history.
      averageSetBitCount = BigInteger.ZERO;
      for (int[] historyEntry : stateHistory) {
        averageSetBitCount = averageSetBitCount.add(getSetBitCount(historyEntry));
      }
      averageSetBitCount = averageSetBitCount.divide(BigInteger.valueOf(stateHistory.size()));
    }
    return averageSetBitCount.intValue();
  }

  public static BigInteger getSetBitCount(int[] array) {
    BigInteger setBitCount = BigInteger.ZERO;
    for (int idx = 0; idx < array.length; idx++) {
      setBitCount = setBitCount.add(BigInteger.valueOf(getSetBitCount(array[idx])));
    }
    return setBitCount;
  }

  public static int getSetBitCount(int value) {
    int setBitCount = 0;
    for (int cnt = 1; cnt <= 32; cnt++) {
      if ((value & 0b1) == 1) {
        setBitCount++;
      }
      value >>= 1;
    }
    return setBitCount;
  }

  public static int getDifferenceInBits(int[] state1, int[] state2) {
    Preconditions.checkArgument(state1.length == state2.length);

    int difference = 0;
    for (int idx = 0; idx < state1.length; idx++) {
      int value1 = state1[idx];
      int value2 = state2[idx];
      if (value1 != value2) {
        difference += getDifferenceInBits(value1, value2);
      }
    }
    return difference;
  }

  public static int getDifferenceInBits(int value1, int value2) {
    int difference = 0;
    for (int cnt = 1; cnt <= 32; cnt++) {
      if ((value1 & 0b1) != (value2 & 0b1)) {
        difference++;
      }
      value1 >>= 1;
      value2 >>= 1;
    }
    return difference;
  }

  public int getElementOutputStateSize() {
    return circuitModel.getElementOutputStateSize();
  }
}