package org.mechaverse.simulation.common.cellautomaton.genetic;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Preconditions;

/**
 * Mutates the groups of {@link CellularAutomatonGeneticData}.
 */
public class CellularAutomatonMutator {

  // TODO(thorntonv): Implement unit test for this class.

  private static final double DEFAULT_MUTATION_PROBABILITY = .001;

  private final double groupMutationProbability;

  public CellularAutomatonMutator() {
    this(DEFAULT_MUTATION_PROBABILITY);
  }

  public CellularAutomatonMutator(double groupMutationProbability) {
    Preconditions.checkArgument(groupMutationProbability >= 0.0f && groupMutationProbability <= 1.0f);
    this.groupMutationProbability = groupMutationProbability;
  }

  public void mutate(CellularAutomatonGeneticData geneticData, RandomGenerator random) {
    Preconditions.checkNotNull(geneticData);
    Preconditions.checkNotNull(random);

    int cellCount = geneticData.getRowCount() * geneticData.getColumnCount();
    int numGroupsToMutate = getNumGroupsToMutate(cellCount, random);

    for (int cnt = 1; cnt <= numGroupsToMutate; cnt++) {
      // Mutate one of the groups at random.
      int row = random.nextInt(geneticData.getRowCount());
      int col = random.nextInt(geneticData.getColumnCount());
      mutateGroup(row, col, geneticData, random);
    }
  }

  private void mutateGroup(int row, int col, CellularAutomatonGeneticData geneticData,
      RandomGenerator random) {
    int neighborRow = row + random.nextInt(3) - 1;
    int neighborCol = col + random.nextInt(3) - 1;

    if (neighborRow > 0 && neighborCol > 0
        && neighborRow < geneticData.getRowCount() && neighborCol < geneticData.getColumnCount()) {
      int group = geneticData.getCrossoverGroup(row, col);
      int neighborGroup = geneticData.getCrossoverGroup(neighborRow, neighborCol);
      geneticData.setCrossoverGroup(neighborGroup, row, col);
      geneticData.setCrossoverGroup(group, neighborRow, neighborCol);
    }
  }

  private int getNumGroupsToMutate(int cellCount, RandomGenerator random) {
    if (groupMutationProbability == 0) {
      return 0;
    } else if (groupMutationProbability == 1) {
      return cellCount;
    }

    return new BinomialDistribution(random, cellCount, groupMutationProbability).sample();
  }
}
