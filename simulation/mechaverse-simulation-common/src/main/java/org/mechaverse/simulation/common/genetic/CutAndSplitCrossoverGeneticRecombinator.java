package org.mechaverse.simulation.common.genetic;

import gnu.trove.list.array.TIntArrayList;

import java.io.ByteArrayOutputStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.util.RandomUtil;

import com.google.common.base.Preconditions;

/**
 * Performs genetic recombination using cut and split crossover.
 */
public class CutAndSplitCrossoverGeneticRecombinator
    implements GeneticRecombinator<CrossoverGeneticData> {

  private static class RecombinationState extends CrossoverGeneticData {

    private int position;
    private int nextCrossoverPointIdx;

    public RecombinationState(CrossoverGeneticData data) {
      super(data.getData(), data.getCrossoverPoints());
    }

    public boolean hasNextCrossoverPoint() {
      return position < data.length;
    }

    public void nextCrossoverPoint() {
      nextCrossoverPoint(null);
    }

    public void nextCrossoverPoint(ByteArrayOutputStream out) {
      int nextCrossoverPoint = (nextCrossoverPointIdx < crossoverPoints.length) ?
          crossoverPoints[nextCrossoverPointIdx] : data.length;
      while (position < data.length && position < nextCrossoverPoint) {
        if(out != null) {
          out.write(data[position]);
        }
        position++;
      }
      if (nextCrossoverPointIdx < crossoverPoints.length) {
        nextCrossoverPointIdx++;
      }
    }
  }

  @Override
  public CrossoverGeneticData recombine(
      CrossoverGeneticData parent1Data, CrossoverGeneticData parent2Data, RandomGenerator random) {
    Preconditions.checkNotNull(parent1Data);
    Preconditions.checkNotNull(parent2Data);
    Preconditions.checkNotNull(random);

    ByteArrayOutputStream childData = new ByteArrayOutputStream(
        Math.max(parent1Data.getData().length, parent2Data.getData().length));
    TIntArrayList childCrossoverPoints = new TIntArrayList(
        Math.max(parent1Data.crossoverPoints.length, parent2Data.crossoverPoints.length));

    RecombinationState parent1 = new RecombinationState(parent1Data);
    RecombinationState parent2 = new RecombinationState(parent2Data);

    while(parent1.hasNextCrossoverPoint() || parent2.hasNextCrossoverPoint()) {
      // Choose a parent at random.
      RecombinationState selectedParent = RandomUtil.nextEvent(.5, random) ? parent1 : parent2;
      RecombinationState otherParent = selectedParent == parent1 ? parent2 : parent1;

      // Child will have bytes from the chosen parent until a crossover point is reached.
      selectedParent.nextCrossoverPoint(childData);
      otherParent.nextCrossoverPoint();
      childCrossoverPoints.add(childData.size());
    }
    return new CrossoverGeneticData(childData.toByteArray(), childCrossoverPoints.toArray());
  }
}
