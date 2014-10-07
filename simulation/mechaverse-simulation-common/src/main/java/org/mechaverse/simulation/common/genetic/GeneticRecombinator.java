package org.mechaverse.simulation.common.genetic;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * An interface for classes that perform genetic recombination.
 *
 * @author Vance Thornton
 */
public interface GeneticRecombinator {

  /**
   * Performs genetic recombination with the given parent data to form the resultant child data.
   */
  GeneticData recombine(GeneticData parent1Data, GeneticData parent2Data, RandomGenerator random);
}
