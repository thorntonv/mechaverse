package org.mechaverse.simulation.common.genetic;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * An interface for classes that perform genetic recombination.
 *
 * @param <T> the genetic data type
 */
public interface GeneticRecombinator<T extends GeneticData> {

  /**
   * Performs genetic recombination with the given parent data to form the resultant child data.
   */
  T recombine(T parent1Data, T parent2Data, RandomGenerator random);

}
