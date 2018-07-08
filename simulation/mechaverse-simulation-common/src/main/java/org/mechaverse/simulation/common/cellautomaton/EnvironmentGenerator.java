package org.mechaverse.simulation.common.cellautomaton;

import java.util.List;

import java.util.Optional;
import org.apache.commons.math3.random.RandomGenerator;


/**
 * An environment generator.
 *
 * @param <E> the environment type
 * @param <T> the entity type
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface EnvironmentGenerator<E, T> {

  /**
   * Generates a local portion of the environment.
   *
   * @param <T> the entity type
   */
  interface LocalGenerator<T> {

    int getWidth();
    int getHeight();
    Optional<T> generateEntity(int row, int column);
  }

  /**
   * Generates a new environment with the given dimensions.
   */
  E generate(int width, int height, RandomGenerator random);

  /**
   * Applies a {@link LocalGenerator} to the environment at the given coordinate.
   *
   * @return a list that contains all generated entities
   */
  List<T> apply(
      LocalGenerator<T> localGenerator, E env, int row, int column, RandomGenerator random);
}
