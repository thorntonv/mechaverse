package org.mechaverse.simulation.common;

import com.google.common.base.Function;

/**
 * Fitness function utility methods.
 *  
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class FitnessFunctions {

  /**
   * Returns a fitness function that always returns a value of 0.
   */
  public static <E extends AbstractEntity> Function<E, Double> zero() {
    return new Function<E, Double>() {
      @Override
      public Double apply(E entity) {
        return 0.0d;
      }
    };
  }
}
