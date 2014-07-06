package org.mechaverse.simulation.common.genetic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link BitMutator}.
 */
public class BitMutatorTest {

  private Random random;

  @Before
  public void setUp() {
    random = new Random(BitMutatorTest.class.getName().hashCode());
  }

  @Test
  public void mutate_noBitsMutated() {
    Mutator mutator = new BitMutator(0.001f);

    byte[] unmodifiedData = new byte[10];
    random.nextBytes(unmodifiedData);
    byte[] data = Arrays.copyOf(unmodifiedData, unmodifiedData.length);
    mutator.mutate(data, random);
    assertArrayEquals(unmodifiedData, data);
  }

  @Test
  public void mutate_bitsMutated() {
    Mutator mutator = new BitMutator(0.001f);

    byte[] unmodifiedData = new byte[1024];
    random.nextBytes(unmodifiedData);
    byte[] data = Arrays.copyOf(unmodifiedData, unmodifiedData.length);
    mutator.mutate(data, random);
    assertFalse(Arrays.equals(unmodifiedData, data));
  }

  @Test
  public void mutate_bitMutationProbabilityZero() {
    Mutator mutator = new BitMutator(0.0f);

    byte[] unmodifiedData = new byte[1024];
    random.nextBytes(unmodifiedData);
    byte[] data = Arrays.copyOf(unmodifiedData, unmodifiedData.length);
    mutator.mutate(data, random);
    assertArrayEquals(unmodifiedData, data);
  }

  @Test
  public void mutate_bitMutationProbabilityOne() {
    Mutator mutator = new BitMutator(1.0f);

    byte[] expectedDataData = new byte[1024];
    random.nextBytes(expectedDataData);
    byte[] data = Arrays.copyOf(expectedDataData, expectedDataData.length);
    mutator.mutate(data, random);

    // Invert all the bits in the expected data.
    for(int idx = 0; idx < data.length; idx++) {
      expectedDataData[idx] ^= (byte) 0xFF;
    }
    assertArrayEquals(expectedDataData, data);
  }

  @Test
  public void mutateBit() {
    byte[] data = new byte[] {0, 0};
    BitMutator.mutateBit(data, 0);
    assertEquals((byte) 0b1, data[0]);
    assertEquals((byte) 0b0, data[1]);

    data = new byte[] {0, 0};
    BitMutator.mutateBit(data, 3);
    assertEquals((byte) 0b1000, data[0]);
    assertEquals((byte) 0b0, data[1]);

    data = new byte[] {0, 0};
    BitMutator.mutateBit(data, 7);
    assertEquals((byte) 0b10000000, data[0]);
    assertEquals((byte) 0b0, data[1]);

    data = new byte[] {0, 0};
    BitMutator.mutateBit(data, 8);
    assertEquals((byte) 0b0, data[0]);
    assertEquals((byte) 0b1, data[1]);

    data = new byte[] {0, 0};
    BitMutator.mutateBit(data, 12);
    assertEquals((byte) 0b0, data[0]);
    assertEquals((byte) 0b10000, data[1]);

    data = new byte[] {0, 0};
    BitMutator.mutateBit(data, 15);
    assertEquals((byte) 0b0, data[0]);
    assertEquals((byte) 0b10000000, data[1]);
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void mutateBit_invalidIndex() {
    byte[] data = new byte[] {0, 0};
    BitMutator.mutateBit(data, 16);
    assertEquals((byte) 0b0, data[0]);
    assertEquals((byte) 0b10000000, data[1]);
  }
}
