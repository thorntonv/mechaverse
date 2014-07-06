package org.mechaverse.simulation.common.genetic;

/**
 * Genetic data that includes crossover points.
 *
 * @author thorntonv@mechaverse.org
 */
public class CrossoverGeneticData extends GeneticData {

  protected final int[] crossoverPoints;

  public CrossoverGeneticData(byte[] data, int[] crossoverPoints) {
    super(data);
    this.crossoverPoints = crossoverPoints;
  }

  public int[] getCrossoverPoints() {
    return crossoverPoints;
  }
}
