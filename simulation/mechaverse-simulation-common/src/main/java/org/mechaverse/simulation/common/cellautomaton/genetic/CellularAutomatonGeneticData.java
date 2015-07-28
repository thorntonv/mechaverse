package org.mechaverse.simulation.common.cellautomaton.genetic;

import java.util.Arrays;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.CellInfo;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.util.ArrayUtil;

import com.google.common.base.Preconditions;

/**
 * Genetic data for a {@link CellularAutomaton}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CellularAutomatonGeneticData extends GeneticData {

  public static class CellGeneticData {

    private final int[] data;

    public CellGeneticData(int[] data) {
      this.data = data;
    }

    public int[] getData() {
      return data;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      CellGeneticData other = (CellGeneticData) obj;
      if (!Arrays.equals(data, other.data)) {
        return false;
      }
      return true;
    }
  }

  private final int rowCount;
  private final int colCount;
  private final CellGeneticData[][] cellData;
  private final int[][] cellGroups;
  private final int[][] cellGroupIndices;

  public CellularAutomatonGeneticData(CellGeneticData[][] cellData, int[][] cellGroups) {
    super(toGeneticData(cellData, cellGroups));

    this.rowCount = cellData.length;
    this.colCount = cellData[0].length;
    this.cellData = cellData;
    this.cellGroups = cellGroups;
    this.cellGroupIndices = buildCellGroupIndices();
  }

  public CellularAutomatonGeneticData(GeneticData geneticData,
      CellularAutomatonSimulationModel model) {
    this(geneticData.getData(), geneticData.getCrossoverGroups(), model);
  }

  public CellularAutomatonGeneticData(byte[] dataBytes, int[] crossoverData,
      CellularAutomatonSimulationModel model) {
    this(toCellGeneticData(dataBytes, crossoverData, model));
  }

  private CellularAutomatonGeneticData(CellularAutomatonGeneticData geneticData) {
    super(geneticData.getData(), geneticData.getCrossoverGroups(),
        geneticData.getCrossoverSplitPoints());
    this.rowCount = geneticData.rowCount;
    this.colCount = geneticData.colCount;
    this.cellData = geneticData.cellData;
    this.cellGroups = geneticData.cellGroups;
    this.cellGroupIndices = buildCellGroupIndices();
  }

  public int getRowCount() {
    return rowCount;
  }

  public int getColumnCount() {
    return colCount;
  }

  public CellGeneticData getCellData(int row, int column) {
    return cellData[row][column];
  }

  public int getCrossoverGroup(int row, int column) {
    return cellGroups[row][column];
  }

  public void setCrossoverGroup(int group, int row, int column) {
    cellGroups[row][column] = group;
    crossoverGroups[cellGroupIndices[row][column]] = group;
  }

  private static GeneticData toGeneticData(CellGeneticData[][] cellData, int[][] cellGroups) {
    GeneticData.Builder builder = GeneticData.newBuilder();
    for (int row = 0; row < cellGroups.length; row++) {
      for (int col = 0; col < cellGroups[row].length; col++) {
        for (int value : cellData[row][col].getData()) {
          builder.writeInt(value, cellGroups[row][col]);
        }
        builder.markSplitPoint();
      }
    }

    return builder.build();
  }

  private static CellularAutomatonGeneticData toCellGeneticData(
      byte[] dataBytes, int[] crossoverGroupData, CellularAutomatonSimulationModel model) {
    Preconditions.checkArgument(dataBytes.length == model.getStateSize() * BYTES_PER_INT);
    Preconditions.checkArgument(crossoverGroupData.length == dataBytes.length);

    int rowCount = model.getHeight() * model.getLogicalUnitInfo().getHeight();
    int colCount = model.getWidth() * model.getLogicalUnitInfo().getWidth();

    CellGeneticData[][] cellData = new CellGeneticData[rowCount][colCount];
    int[][] cellGroups = new int[rowCount][colCount];

    int[] data = ArrayUtil.toIntArray(dataBytes);
    int dataIdx = 0;
    int crossoverDataIdx = 0;
    for (int row = 0; row < rowCount; row++) {
      for (int col = 0; col < colCount; col++) {
        CellInfo cellInfo = model.getCell(row, col);
        int[] cellState = new int[cellInfo.getStateSize()];
        for (int idx = 0; idx < cellState.length; idx++) {
          cellState[idx] = data[dataIdx++];
        }
        cellData[row][col] = new CellGeneticData(cellState);
        cellGroups[row][col] = crossoverGroupData[crossoverDataIdx];
        crossoverDataIdx += cellState.length * BYTES_PER_INT;
      }
    }
    return new CellularAutomatonGeneticData(cellData, cellGroups);
  }

  private int[][] buildCellGroupIndices() {
    int[][] cellGroupIndices = new int[rowCount][colCount];
    int idx = 0;
    for (int row = 0; row < rowCount; row++) {
      for (int col = 0; col < colCount; col++) {
        cellGroupIndices[row][col] = idx;
        idx += cellData[row][col].data.length * BYTES_PER_INT;
      }
    }
    return cellGroupIndices;
  }
}
