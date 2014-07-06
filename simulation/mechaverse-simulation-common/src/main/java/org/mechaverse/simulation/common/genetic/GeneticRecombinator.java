package org.mechaverse.simulation.common.genetic;

import java.util.Random;

/**
 * An interface for classes that perform genetic recombination.
 *
 * @author thorntonv@mechaverse.org
 *
 * @param <T> the genetic data type
 */
public interface GeneticRecombinator<T extends GeneticData> {

  /**
   * Performs genetic recombination with the given parent data to form the resultant child data.
   */
  T recombine(T parent1Data, T parent2Data, Random random);

}
