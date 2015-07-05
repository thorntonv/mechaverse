package org.mechaverse.simulation.common.cellautomaton.genetic;

import static org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.CONSTANT_TYPE;
import static org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.TOGGLE_TYPE;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticData.CellGeneticData;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.ConstantCellType;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.ToggleCellType;
import org.mechaverse.simulation.common.util.ArrayUtil;

import com.google.common.collect.ImmutableList;

public class CellularAutomatonGeneticDataTestUtil {

  public static final CellularAutomatonDescriptor TEST_DESCRIPTOR =
      CellularAutomatonBuilder.newCellularAutomaton(1, 1,
          ImmutableList.of(ConstantCellType.newInstance(), ToggleCellType.newInstance()),
              new String[][]{{CONSTANT_TYPE, TOGGLE_TYPE}, {TOGGLE_TYPE, CONSTANT_TYPE}});

  public static final CellGeneticData[][] TEST_CELL_DATA = new CellGeneticData[][] {
      {new CellGeneticData(new int[] {0, 1}), new CellGeneticData(new int[] {2, 3, 4})},
      {new CellGeneticData(new int[] {5, 6, 7}), new CellGeneticData(new int[] {8, 9})}
  };

  public static final int[][] TEST_CELL_GROUPS = new int[][] { {0, 1}, {2, 3} };

  public static final byte[] TEST_GENETIC_DATA = ArrayUtil.toByteArray(
      new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

  public static final int[] TEST_CROSSOVER_GROUPS = new int[] {
      0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3
  };

  public static final int[] TEST_CROSSOVER_SPLIT_POINTS =
      new int[] {4 * 2, (4 * 2) + 4 * 3, (4 * 2 + 4 * 3) + 4 * 3, (4 * 2 + 4 * 3 + 4 * 3) + 4 * 2};

}
