package org.mechaverse.simulation.common.cellautomaton.genetic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataTestUtil.TEST_CELL_DATA;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataTestUtil.TEST_CELL_GROUPS;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataTestUtil.TEST_CROSSOVER_GROUPS;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataTestUtil.TEST_CROSSOVER_SPLIT_POINTS;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataTestUtil.TEST_DESCRIPTOR;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataTestUtil.TEST_GENETIC_DATA;

import org.junit.Test;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;

/**
 * Unit test for {@link CellularAutomatonGeneticData}.
 */
public class CellularAutomatonGeneticDataTest {

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
