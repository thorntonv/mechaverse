package org.mechaverse.simulation.common.genetic;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Preconditions;

/**
 * Mutates bits in a byte array at random with a given probability.
 */
public class BitMutator implements Mutator {

  private static final int BITS_PER_BYTE = Byte.SIZE;
  private static final int BITS_PER_INT = Integer.SIZE;

  private final double bitMutationProbability;

  public BitMutator(double bitMutationProbability) {
    Preconditions.checkArgument(bitMutationProbability >= 0.0f && bitMutationProbability <= 1.0f);
    this.bitMutationProbability = bitMutationProbability;
  }

  @Override
  public void mutate(byte[] data, RandomGenerator random) {
    Preconditions.checkNotNull(data);
    Preconditions.checkNotNull(random);

    int bitCount = data.length * BITS_PER_BYTE;
    int numBitsToMutate = getNumBitsToMutate(bitCount, random);

    TIntSet mutatedBits = new TIntHashSet(numBitsToMutate);
    for (int cnt = 1; cnt <= numBitsToMutate; cnt++) {
      // Mutate one of the remaining bits at random.
      int bitToMutateIdx = random.nextInt(bitCount);
      while (mutatedBits.contains(bitToMutateIdx)) {
        bitToMutateIdx = random.nextInt(bitCount);
      }
      mutateBit(data, bitToMutateIdx);
      mutatedBits.add(bitToMutateIdx);
    }
  }

  public void mutate(int[] data, RandomGenerator random) {
    Preconditions.checkNotNull(data);
    Preconditions.checkNotNull(random);

    int bitCount = data.length * BITS_PER_INT;
    int numBitsToMutate = getNumBitsToMutate(bitCount, random);

    TIntSet mutatedBits = new TIntHashSet(numBitsToMutate);
    for (int cnt = 1; cnt <= numBitsToMutate; cnt++) {
      // Mutate one of the remaining bits at random.
      int bitToMutateIdx = random.nextInt(bitCount);
      while (mutatedBits.contains(bitToMutateIdx)) {
        bitToMutateIdx = random.nextInt(bitCount);
      }
      mutateBit(data, bitToMutateIdx);
      mutatedBits.add(bitToMutateIdx);
    }
  }

  public static void mutateBit(byte[] data, int bitIndex) {
    int arrayIndex = bitIndex / BITS_PER_BYTE;
    int bitInByte = bitIndex % BITS_PER_BYTE;
    data[arrayIndex] ^= (1 << bitInByte);
  }

  public static void mutateBit(int[] data, int bitIndex) {
    int arrayIndex = bitIndex / BITS_PER_INT;
    int bitInInt = bitIndex % BITS_PER_INT;
    data[arrayIndex] ^= (1 << bitInInt);
  }

  private int getNumBitsToMutate(int bitCount, RandomGenerator random) {
    if (bitMutationProbability == 0) {
      return 0;
    } else if (bitMutationProbability == 1) {
      return bitCount;
    }

    return new BinomialDistribution(random, bitCount, bitMutationProbability).sample();
  }
}
