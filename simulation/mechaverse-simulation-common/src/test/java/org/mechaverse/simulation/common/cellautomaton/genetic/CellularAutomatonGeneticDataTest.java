package org.mechaverse.simulation.common.cellautomaton.genetic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticData.CellGeneticData;
import org.mechaverse.simulation.common.util.ArrayUtil;

/**
 * Unit test for {@link CellularAutomatonGeneticData}.
 */
public class CellularAutomatonGeneticDataTest {

  private static final int TEST_ROW_COUNT = 2;
  private static final int TEST_COL_COUNT = 2;
  private static final int TEST_CELL_VALUE_COUNT = 2;

  private static final CellGeneticData[][] TEST_CELL_DATA = new CellGeneticData[][] {
      { new CellGeneticData(new int[] {0, 1}), new CellGeneticData(new int[] {2, 3}) },
      { new CellGeneticData(new int[] {4, 5}), new CellGeneticData(new int[] {6, 7}) }
  };

  private static final int[][] TEST_CELL_GROUPS = new int[][] { {0, 1}, {2, 3} };

  private static final byte[] TEST_GENETIC_DATA = ArrayUtil.toByteArray(
      new int[] {0, 1, 2, 3, 4, 5, 6, 7});

  private static final int[] TEST_GENETIC_CROSSOVER_DATA = new int[] {
      0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1,
      2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3
  };

  @Test
  public void testToGeneticData() {
    CellularAutomatonGeneticData geneticData =
        new CellularAutomatonGeneticData(TEST_CELL_DATA, TEST_CELL_GROUPS);

    assertArrayEquals(TEST_GENETIC_DATA, geneticData.getData());
    assertArrayEquals(TEST_GENETIC_CROSSOVER_DATA, geneticData.getCrossoverData());
  }

  @Test
  public void testToCellGeneticData() {
    CellularAutomatonGeneticData geneticData = new CellularAutomatonGeneticData(TEST_GENETIC_DATA,
        TEST_GENETIC_CROSSOVER_DATA, TEST_ROW_COUNT, TEST_COL_COUNT, TEST_CELL_VALUE_COUNT);

    for (int row = 0; row < TEST_ROW_COUNT; row++) {
      for (int col = 0; col < TEST_COL_COUNT; col++) {
        CellGeneticData expectedCellData = TEST_CELL_DATA[row][col];
        CellGeneticData cellData = geneticData.getCellData(row, col);
        assertArrayEquals(expectedCellData.getData(), cellData.getData());
        assertEquals(TEST_CELL_GROUPS[row][col], geneticData.getCrossoverGroup(row, col));
      }
    }
  }
}
