package org.mechaverse.simulation.common;

import java.util.List;
import java.util.Optional;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;


/**
 * Generates a random environment model.
 *
 * @param <ENV_MODEL> the environment model type
 * @param <ENT_MODEL> the entity model type
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface EnvironmentModelGenerator<
    ENV_MODEL extends EnvironmentModel,
    ENT_MODEL extends EntityModel,
    ENT_TYPE extends Enum<ENT_TYPE>> {

  /**
   * Generates a local portion of the environment.
   *
   * @param <ENT_TYPE> the entity model type
   */
  interface LocalGenerator<ENT_TYPE extends Enum<ENT_TYPE>> {

    int getWidth();
    int getHeight();
    Optional<ENT_TYPE> generateEntity(int row, int column);
  }

  /**
   * Generates a new environment with the default dimensions.
   */
  ENV_MODEL generate(RandomGenerator random);

  /**
   * Generates a new environment with the given dimensions.
   */
  ENV_MODEL generate(int width, int height, RandomGenerator random);

  /**
   * Applies a {@link LocalGenerator} to the environment at the given coordinate.
   *
   * @return a list that contains all generated entities
   */
  List<ENT_MODEL> apply(
      LocalGenerator<ENT_TYPE> localGenerator, ENV_MODEL env, int row, int column, RandomGenerator random);
}
