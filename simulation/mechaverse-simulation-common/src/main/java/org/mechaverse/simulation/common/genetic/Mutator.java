package org.mechaverse.simulation.common.genetic;

import java.util.Random;

/**
 * Mutates bits in a byte array.
 *
 * @author thorntonv@mechaverse.org
 */
public interface Mutator {

  /**
   * Mutates bytes in the given array.
   */
  void mutate(byte[] data, Random random);
}
