package org.mechaverse.simulation.common.genetic;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * A unit test for {@link CutAndSpliceCrossoverGeneticRecombinator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CutAndSpliceCrossoverGeneticRecombinatorTest {

  private static final GeneticData TEST_PARENT1_DATA =
      new GeneticData(new byte[] {50, 72, 21, 35}, new int[] {00, 01, 00, 00});
  private static final GeneticData TEST_PARENT2_DATA =
      new GeneticData(new byte[] {39, 12, 96, 77}, new int[] {00, 00, 00, 01});

  @Mock BitMutator mockBitMutator;
  @Mock private RandomGenerator mockRandom;

  private RandomGenerator random;

  @Before
  public void setUp() {
    random = RandomUtil.newGenerator(
        CutAndSpliceCrossoverGeneticRecombinatorTest.class.getName().hashCode());
  }

  /**
   * Implements the example from the {@link CutAndSpliceCrossoverGeneticRecombinator} java doc.
   */
  @Test
  public void recombine() {
    BitMutator mutator = null;
    GeneticRecombinator recombinator = new CutAndSpliceCrossoverGeneticRecombinator(mutator);

    when(mockRandom.nextDouble())
        // Index 0: Choose parent at random and assign to group 0. Parent 2 is assigned to group 0.
        .thenReturn(.75)
        // Index 1: Choose parent at random and use its group assignment.
        // Group assignment from parent 1 is used.
        .thenReturn(.25)
        // Index 1: Choose parent at random and assign to group 1. Parent 1 is assigned to group 1.
        .thenReturn(.25)
        // Index 3: Choose parent at random and use its group assignment.
        // Group assignment from parent 2 is used.
        .thenReturn(.75);

    GeneticData childData =
        recombinator.recombine(TEST_PARENT1_DATA, TEST_PARENT2_DATA, mockRandom);

    assertArrayEquals(new byte[] {39, 72, 96, 35}, childData.getData());
    assertArrayEquals(new int[] {00, 01, 00, 01}, childData.getCrossoverData());
  }

  /**
   * Verifies that the mutate method is called when a {@link BitMutator} is provided.
   */
  @Test
  public void recombine_mutate() {
    GeneticRecombinator recombinator = new CutAndSpliceCrossoverGeneticRecombinator(mockBitMutator);

    GeneticData childData = recombinator.recombine(TEST_PARENT1_DATA, TEST_PARENT2_DATA, random);

    verify(mockBitMutator).mutate(childData.getData(), random);
  }
}
