package org.mechaverse.simulation.common.genetic;

/**
 * Mutates bits in a byte array.
 *
 * @author thorntonv@mechaverse.org
 */
public interface Mutator {

  /**
   * Mutates bytes in the given array.
   */
  public void mutate(byte[] data);
}
