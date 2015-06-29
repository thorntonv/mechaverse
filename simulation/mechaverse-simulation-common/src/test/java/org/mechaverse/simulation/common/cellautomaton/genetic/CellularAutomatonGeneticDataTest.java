package org.mechaverse.simulation.common.cellautomaton.genetic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.CONSTANT_TYPE;
import static org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.TOGGLE_TYPE;

import org.junit.Test;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticData.CellGeneticData;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.ConstantCellType;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.ToggleCellType;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.mechaverse.simulation.common.util.ArrayUtil;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for {@link CellularAutomatonGeneticData}.
 */
public class CellularAutomatonGeneticDataTest {

  private static final CellularAutomatonDescriptor TEST_DESCRIPTOR =
      CellularAutomatonBuilder.newCellularAutomaton(1, 1,
          ImmutableList.of(ConstantCellType.newInstance(), ToggleCellType.newInstance()),
              new String[][]{{CONSTANT_TYPE, TOGGLE_TYPE}, {TOGGLE_TYPE, CONSTANT_TYPE}});

  private static final CellGeneticData[][] TEST_CELL_DATA = new CellGeneticData[][] {
      {new CellGeneticData(new int[] {0, 1}), new CellGeneticData(new int[] {2, 3, 4})},
      {new CellGeneticData(new int[] {5, 6, 7}), new CellGeneticData(new int[] {8, 9})}
  };

  private static final int[][] TEST_CELL_GROUPS = new int[][] { {0, 1}, {2, 3} };

  private static final byte[] TEST_GENETIC_DATA = ArrayUtil.toByteArray(
      new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

  private static final int[] TEST_CROSSOVER_GROUPS = new int[] {
      0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3
  };

  private static final int[] TEST_CROSSOVER_SPLIT_POINTS =
      new int[] {4 * 2, (4 * 2) + 4 * 3, (4 * 2 + 4 * 3) + 4 * 3, (4 * 2 + 4 * 3 + 4 * 3) + 4 * 2};

  @Test
  public void testToGeneticData() {
    CellularAutomatonGeneticData geneticData =
        new CellularAutomatonGeneticData(TEST_CELL_DATA, TEST_CELL_GROUPS);

    assertArrayEquals(TEST_GENETIC_DATA, geneticData.getData());
    assertArrayEquals(TEST_CROSSOVER_GROUPS, geneticData.getCrossoverGroups());
    assertArrayEquals(TEST_CROSSOVER_SPLIT_POINTS, geneticData.getCrossoverSplitPoints());
  }

  @Test
  public void testToCellGeneticData() {
    CellularAutomatonSimulationModel model =
        CellularAutomatonSimulationModelBuilder.build(TEST_DESCRIPTOR);
    CellularAutomatonGeneticData geneticData = new CellularAutomatonGeneticData(
        TEST_GENETIC_DATA, TEST_CROSSOVER_GROUPS, model);

    int rowCount = model.getHeight() * model.getLogicalUnitInfo().getHeight();
    int colCount = model.getWidth() * model.getLogicalUnitInfo().getWidth();

    assertEquals(rowCount, geneticData.getRowCount());
    assertEquals(colCount, geneticData.getColumnCount());

    for (int row = 0; row < rowCount; row++) {
      for (int col = 0; col < colCount; col++) {
        assertEquals(TEST_CELL_DATA[row][col], geneticData.getCellData(row, col));
        assertEquals(TEST_CELL_GROUPS[row][col], geneticData.getCrossoverGroup(row, col));
      }
    }

    assertArrayEquals(TEST_CROSSOVER_SPLIT_POINTS, geneticData.getCrossoverSplitPoints());
  }
}
