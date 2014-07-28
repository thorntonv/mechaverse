package org.mechaverse.simulation.common.util;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Random number generation utility methods.
 */
public final class RandomUtil {

  private RandomUtil() {}

  /**
   * Generates an event according the given probability value.
   * 
   * @param p the probability that the event will occur
   * @param random A source of randomness for generating events.
   * 
   * @return true with a probability of p, false with a probability of 1-p.
   */
  public static boolean nextEvent(double p, RandomGenerator random) {
    if (p == 1) {
      return true;
    } else if (p == 0) {
      return false;
    } else {
      return random.nextDouble() <= p;
    }
  }

  /**
   * Generates an event according the given probability value.
   * 
   * @param p the probability that the event will occur
   * @param random A source of randomness for generating events.
   * 
   * @return true with a probability of p, false with a probability of 1-p.
   */
  public static boolean nextEvent(float p, RandomGenerator random) {
    if (p == 1) {
      return true;
    } else if (p == 0) {
      return false;
    } else {
      return random.nextFloat() <= p;
    }
  }
}
