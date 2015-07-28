package org.mechaverse.simulation.common.cellautomaton.genetic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataTestUtil.TEST_CELL_DATA;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataTestUtil.TEST_CROSSOVER_SPLIT_POINTS;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataTestUtil.TEST_DESCRIPTOR;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataTestUtil.TEST_GENETIC_DATA;
import static org.mockito.Mockito.when;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.ConstantCellType;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CellularAutomatonGeneticDataGeneratorTest {

  private static final int[][] TEST_GROUPS = new int[][] {
      {0, 0, 1, 1, 2, 2},
      {0, 0, 1, 1, 2, 2},
      {3, 3, 4, 4, 5, 5},
      {3, 3, 4, 4, 5, 5},
      {6, 6, 7, 7, 8, 8},
      {6, 6, 7, 7, 8, 8}};

  @Mock private RandomGenerator mockRandom;

  private CellularAutomatonGeneticDataGenerator generator;

  @Before
  public void setUp() {
    this.generator = new CellularAutomatonGeneticDataGenerator();
  }

  @Test
  public void testGenerateStateGeneticData() {
    CellularAutomatonSimulationModel model = new CellularAutomatonSimulationModelBuilder()
        .buildModel(TEST_DESCRIPTOR);

    when(mockRandom.nextInt()).thenReturn(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

    CellularAutomatonGeneticData geneticData =
        generator.generateStateGeneticData(model, mockRandom);
    assertEquals(model.getHeight() * model.getLogicalUnitInfo().getHeight(), geneticData.getRowCount());
    assertEquals(model.getWidth() * model.getLogicalUnitInfo().getWidth(), geneticData.getColumnCount());
    assertArrayEquals(TEST_GENETIC_DATA, geneticData.getData());
    for (int row = 0; row < geneticData.getRowCount(); row++) {
      for (int col = 0; col < geneticData.getColumnCount(); col++) {
        assertArrayEquals(TEST_CELL_DATA[row][col].getData(),
            geneticData.getCellData(row, col).getData());
      }
    }
    assertArrayEquals(TEST_CROSSOVER_SPLIT_POINTS, geneticData.getCrossoverSplitPoints());
  }

  @Test
  public void testGenerateStateGeneticData_groups() {
    CellularAutomatonDescriptor descriptor = CellularAutomatonBuilder.newCellularAutomaton(
        2, 2, ConstantCellType.newInstance(), 3, 3);
    CellularAutomatonSimulationModel model = new CellularAutomatonSimulationModelBuilder()
        .buildModel(descriptor);

    CellularAutomatonGeneticData geneticData =
        generator.generateStateGeneticData(model, mockRandom);

    assertEquals(6, geneticData.getRowCount());
    assertEquals(6, geneticData.getColumnCount());

    for (int row = 0; row < TEST_GROUPS.length; row++) {
      for (int col = 0; col < TEST_GROUPS[row].length; col++) {
        assertEquals(TEST_GROUPS[row][col], geneticData.getCrossoverGroup(row, col));
      }
    }
  }
}
