package org.mechaverse.simulation.common.util;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

/**
 * Random number generation utility methods.
 */
public final class RandomUtil {

  private RandomUtil() {}

  /**
   * Returns a new random generator.
   */
  public static RandomGenerator newGenerator() {
    // TODO(thorntonv): Consider injecting this implementation.
    return new Well19937c();
  }

  /**
   * Returns a new random generator. Initialized with the given seed.
   */
  public static RandomGenerator newGenerator(long seed) {
    RandomGenerator generator = new Well19937c();
    generator.setSeed(seed);
    return generator;
  }

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

  /**
   * Returns a new random byte array with the given length.
   */
  public static byte[] randomBytes(int length, RandomGenerator random) {
    byte[] data = new byte[length];
    random.nextBytes(data);
    return data;
  }
}
