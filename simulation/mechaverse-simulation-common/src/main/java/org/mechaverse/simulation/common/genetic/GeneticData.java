package org.mechaverse.simulation.common.genetic;

/**
 * Base class for genetic data.
 *
 * @author thorntonv@mechaverse.org
 */
public class GeneticData {

  protected final byte[] data;

  public GeneticData(byte[] data) {
    this.data = data;
  }

  public byte[] getData() {
    return data;
  }
}
