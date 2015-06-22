package org.mechaverse.simulation.common.genetic;

import gnu.trove.map.hash.TIntObjectHashMap;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.util.RandomUtil;

import com.google.common.base.Preconditions;

/**
 * Performs genetic recombination using cut and splice crossover. The crossover data for each parent
 * should be an array where the value at each position is the group id assigned to the corresponding
 * position in the data byte array. For each group a parent is chosen at random and the value from
 * that parent is transferred to the child for each byte in that group. If a different group is
 * assigned to the same byte position for both parents then one of the parents is chosen at random
 * and its group assignment is used.
 * <pre>
 * Example
 *
 * Parent 1 data: 50 72 21 35
 *        groups: 00 01 00 00
 * Parent 2 data: 39 12 96 77
 *        groups: 00 00 00 01
 *
 * Index 0:
 * Choose parent at random and assign to group 00. Parent 2 is assigned to group 00.
 *
 * Child data: 39 __ __ __
 *     groups: 00 __ __ __
 *
 * Index 1:
 * Choose parent at random and use its group assignment. Group assignment from parent 1 is used.
 * Choose parent at random and assign to group 01. Parent 1 is assigned to group 01.
 *
 * Child data: 39 72 __ __
 *     groups: 00 01 __ __
 *
 * Index 2:
 * Use value from parent 2 which is assigned to group 00.
 *
 * Child data: 39 72 96 __
 *     groups: 00 01 00 __
 *
 * Index 3:
 * Choose parent at random and use its group assignment. Group assignment from parent 2 is used.
 * Use value from parent 1 which is assigned to group 01.
 *
 * Child data: 39 72 96 35
 *     groups: 00 01 00 01
 * </pre>
 */
public class CutAndSpliceCrossoverGeneticRecombinator implements GeneticRecombinator {

  private static final float DEFAULT_MUTATION_RATE = .001f;

  private final BitMutator mutator;

  public CutAndSpliceCrossoverGeneticRecombinator() {
    this(new BitMutator(DEFAULT_MUTATION_RATE));
  }

  public CutAndSpliceCrossoverGeneticRecombinator(BitMutator mutator) {
    this.mutator = mutator;
  }

  @Override
  public GeneticData recombine(
      GeneticData parent1, GeneticData parent2, RandomGenerator random) {
    Preconditions.checkNotNull(parent1);
    Preconditions.checkNotNull(parent2);
    Preconditions.checkNotNull(random);

    byte[] parent1Data = parent1.getData();
    int[] parent1CrossoverData = parent1.getCrossoverData();
    byte[] parent2Data = parent2.getData();
    int[] parent2CrossoverData = parent2.getCrossoverData();

    Preconditions.checkArgument(parent1Data.length == parent2Data.length);
    Preconditions.checkArgument(parent1Data.length == parent1CrossoverData.length);
    Preconditions.checkArgument(parent2Data.length == parent2CrossoverData.length);

    byte[] childData = new byte[parent1Data.length];
    int[] childCrossoverData = new int[childData.length];

    TIntObjectHashMap<GeneticData> groupParentMap = new TIntObjectHashMap<GeneticData>();
    for (int idx = 0; idx < childData.length; idx++) {
      int parent1Group = parent1CrossoverData[idx];
      int parent2Group = parent2CrossoverData[idx];

      int group = parent1Group;
      if (parent1Group != parent2Group) {
        group = RandomUtil.nextEvent(.5, random) ? parent1Group : parent2Group;
      }

      GeneticData selectedParent = groupParentMap.get(group);
      if (selectedParent == null) {
        selectedParent = RandomUtil.nextEvent(.5, random) ? parent1 : parent2;
        groupParentMap.put(group, selectedParent);
      }

      childData[idx] = (selectedParent == parent1) ? parent1Data[idx] : parent2Data[idx];
      childCrossoverData[idx] = group;
    }

    if (mutator != null) {
      mutator.mutate(childData, random);
    }

    return new GeneticData(childData, childCrossoverData);
  }
}
