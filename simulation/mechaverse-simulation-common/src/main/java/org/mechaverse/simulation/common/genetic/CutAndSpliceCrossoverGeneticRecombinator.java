package org.mechaverse.simulation.common.genetic;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.util.RandomUtil;

import com.google.common.base.Preconditions;

import gnu.trove.map.hash.TIntObjectHashMap;

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
    int[] parent1Groups = parent1.getCrossoverGroups();
    int[] parent1SplitPoints = parent1.getCrossoverSplitPoints();
    int parent1SplitPointIdx = 0;

    byte[] parent2Data = parent2.getData();
    int[] parent2Groups = parent2.getCrossoverGroups();
    int[] parent2SplitPoints = parent2.getCrossoverSplitPoints();
    int parent2SplitPointIdx = 0;

    Preconditions.checkArgument(parent1Data.length == parent2Data.length);
    Preconditions.checkArgument(parent1Data.length == parent1Groups.length);
    Preconditions.checkArgument(parent2Data.length == parent2Groups.length);

    GeneticData.Builder child = GeneticData.newBuilder();

    TIntObjectHashMap<GeneticData> groupParentMap = new TIntObjectHashMap<GeneticData>();
    for (int idx = 0; idx < parent1Data.length; idx++) {
      int parent1Group = parent1Groups[idx];
      int parent2Group = parent2Groups[idx];

      int group = parent1Group;
      if (parent1Group != parent2Group) {
        group = RandomUtil.nextEvent(.5, random) ? parent1Group : parent2Group;
      }

      GeneticData selectedParent = groupParentMap.get(group);
      if (selectedParent == null) {
        selectedParent = RandomUtil.nextEvent(.5, random) ? parent1 : parent2;
        groupParentMap.put(group, selectedParent);
      }

      byte[] selectedParentData = (selectedParent == parent1) ? parent1Data : parent2Data;

      // Copy data from the selected parent until a split point is reached.
      boolean splitPointReached = false;
      while (idx < selectedParentData.length && !splitPointReached) {
        child.write(selectedParentData[idx], group);

        if (parent1SplitPointIdx < parent1SplitPoints.length
            && parent1SplitPoints[parent1SplitPointIdx] == idx + 1) {
          parent1SplitPointIdx++;
          if (selectedParent == parent1) {
            splitPointReached = true;
          }
        }
        if (parent2SplitPointIdx < parent2SplitPoints.length
            && parent2SplitPoints[parent2SplitPointIdx] == idx + 1) {
          parent2SplitPointIdx++;
          if (selectedParent == parent2) {
            splitPointReached = true;
          }
        }
        if (!splitPointReached) {
          idx++;
        }
      };

      if (idx < selectedParentData.length) {
        child.markSplitPoint();
      }
    }

    GeneticData childGeneticData = child.build();

    if (mutator != null) {
      mutator.mutate(childGeneticData.getData(), random);
    }

    return childGeneticData;
  }
}
