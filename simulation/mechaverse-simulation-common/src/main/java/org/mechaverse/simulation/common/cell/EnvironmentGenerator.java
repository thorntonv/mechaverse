package org.mechaverse.simulation.common.cell;

import java.util.Random;

import com.google.common.base.Optional;

/**
 * An environment generator.
 *
 * @param <E> the environment type
 * @param <T> the entity type
 *
 * @author thorntonv
 */
public interface EnvironmentGenerator<E, T> {

  /**
   * Generates a local portion of the environment.
   *
   * @param <T> the entity type
   */
  public static interface LocalGenerator<T> {

    int getWidth();
    int getHeight();
    Optional<T> generateEntity(int row, int column);
  }

  /**
   * Generates a new environment with the given dimensions.
   */
  E generate(int width, int height, Random random);

  /**
   * Applies a {@link LocalGenerator} to the environment at the given coordinate.
   */
  void apply(LocalGenerator<T> localGenerator, E env, int row, int column, Random random);
}