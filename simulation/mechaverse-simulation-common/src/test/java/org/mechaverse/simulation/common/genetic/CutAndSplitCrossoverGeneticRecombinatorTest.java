package org.mechaverse.simulation.common.genetic;

import static org.junit.Assert.assertEquals;
import gnu.trove.list.array.TIntArrayList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class CutAndSplitCrossoverGeneticRecombinatorTest {

  private Random random;

  @Before
  public void setUp() {
    random = new Random(BitMutatorTest.class.getName().hashCode());
  }

  @Test
  public void recombine() {
    GeneticRecombinator<CrossoverGeneticData> recombinator =
        new CutAndSplitCrossoverGeneticRecombinator();

    ByteArrayOutputStream parent1Out = new ByteArrayOutputStream();
    ByteArrayOutputStream parent2Out = new ByteArrayOutputStream();

    TIntArrayList parent1CrossoverPoints = new TIntArrayList();
    TIntArrayList parent2CrossoverPoints = new TIntArrayList();
    for (int n = 1; n <= 100; n++) {
      // Select parent1 for evens and parent2 for odds
      ByteArrayOutputStream selectedParent = (n % 2 == 0) ? parent1Out : parent2Out;
      TIntArrayList selectParentCrossoverPoints = (n % 2 == 0) ?
          parent1CrossoverPoints : parent2CrossoverPoints;

      // Write the value n, n times to the parent.
      for (int cnt = 1; cnt <= n; cnt++) {
        selectedParent.write(n);
      }
      selectParentCrossoverPoints.add(selectedParent.size());
    }

    CrossoverGeneticData parent1Data =
        new CrossoverGeneticData(parent1Out.toByteArray(), parent1CrossoverPoints.toArray());
    CrossoverGeneticData parent2Data =
        new CrossoverGeneticData(parent2Out.toByteArray(), parent2CrossoverPoints.toArray());

    CrossoverGeneticData childData = recombinator.recombine(parent1Data, parent2Data, random);

    ByteArrayInputStream childIn = new ByteArrayInputStream(childData.getData());

    int pos = 0;
    int crossoverIdx = 0;
    while(childIn.available() > 0) {
      int n = childIn.read();
      pos++;
      // Read the remaining n - 1 copies of n.
      for(int cnt = 1; cnt < n; cnt++) {
        assertEquals(n, childIn.read());
        pos++;
      }
      assertEquals(pos, childData.getCrossoverPoints()[crossoverIdx]);
      crossoverIdx++;
    }

    assertEquals(crossoverIdx, childData.getCrossoverPoints().length);
  }
}
