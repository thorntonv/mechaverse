package org.mechaverse.simulation.common.genetic;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Mutates bits in a byte array.
 */
public interface Mutator {

  /**
   * Mutates bytes in the given array.
   */
  void mutate(byte[] data, RandomGenerator random);
}
