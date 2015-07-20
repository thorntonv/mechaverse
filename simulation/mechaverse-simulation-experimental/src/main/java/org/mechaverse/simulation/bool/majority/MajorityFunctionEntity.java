package org.mechaverse.simulation.bool.majority;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.common.AbstractEntity;
import org.mechaverse.simulation.common.cellautomaton.analysis.CellularAutomatonAnalyzer;

/**
 * An {@link AbstractEntity} implementation for the {@link MajorityFunctionSimulation}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class MajorityFunctionEntity extends AbstractEntity {

  static final int ENTITY_INPUT_SIZE = 1;
  static final int ENTITY_OUTPUT_SIZE = 1;

  private int age = 0;
  private int correct = 0;
  private int[] input = new int[1];
  private RandomGenerator random = new Well19937c();

  @Override
  public int[] getInput() {
    input[0] = random.nextInt(128);
    return input;
  }

  @Override
  public void processOutput(int[] output) {
    int expectedValue = (CellularAutomatonAnalyzer.getSetBitCount(input[0]) > 3) ? 1 : 0;
    if ((output[0] & 1) == expectedValue) {
      correct++;
    }
    age++;
  }

  public int getAge() {
    return age;
  }

  public int getNumCorrect() {
    return correct;
  }

  @Override
  public String toString() {
    return String.format("%s - %d / %d (%f)", getId(), correct, age, correct / (double) age);
  }
}
