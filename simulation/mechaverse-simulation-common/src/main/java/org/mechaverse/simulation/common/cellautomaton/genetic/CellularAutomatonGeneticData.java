package org.mechaverse.simulation.common.cellautomaton.genetic;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
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
  }

  private final int rowCount;
  private final int colCount;
  private final CellGeneticData[][] cellData;
  private final int[][] cellGroups;

  public CellularAutomatonGeneticData(CellGeneticData[][] cellData, int[][] cellGroups) {
    super(toGeneticData(cellData, cellGroups));

    this.rowCount = cellData.length;
    this.colCount = cellData[0].length;
    this.cellData = cellData;
    this.cellGroups = cellGroups;
  }

  public CellularAutomatonGeneticData(byte[] dataBytes, int[] crossoverData,
      int rowCount, int colCount, int cellValueCount) {
    this(toCellGeneticData(dataBytes, crossoverData, rowCount, colCount, cellValueCount));
  }

  private CellularAutomatonGeneticData(CellularAutomatonGeneticData geneticData) {
    super(geneticData.getData(), geneticData.getCrossoverData());
    this.rowCount = geneticData.rowCount;
    this.colCount = geneticData.colCount;
    this.cellData = geneticData.cellData;
    this.cellGroups = geneticData.cellGroups;
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

  private static GeneticData toGeneticData(CellGeneticData[][] cellData, int[][] cellGroups) {
    int rowCount = cellGroups.length;
    int colCount = cellGroups[0].length;
    int cellValuesCount = cellData[0][0].getData().length;

    int[] data = new int[rowCount * colCount * cellValuesCount];
    int[] crossoverData = new int[data.length * BYTES_PER_INT];

    int dataIdx = 0;
    int crossoverDataIdx = 0;
    for (int row = 0; row < rowCount; row++) {
      for (int col = 0; col < colCount; col++) {
        for (int value : cellData[row][col].getData()) {
          data[dataIdx++] = value;
          for (int cnt = 0; cnt < BYTES_PER_INT; cnt++) {
            crossoverData[crossoverDataIdx++] = cellGroups[row][col];
          }
        }
      }
    }
    return new GeneticData(ArrayUtil.toByteArray(data), crossoverData);
  }

  private static CellularAutomatonGeneticData toCellGeneticData(
      byte[] dataBytes, int[] crossoverData, int rowCount, int colCount, int cellValueCount) {
    Preconditions.checkArgument(
        dataBytes.length == rowCount * colCount * cellValueCount * BYTES_PER_INT);
    Preconditions.checkArgument(crossoverData.length == dataBytes.length);

    CellGeneticData[][] cellData = new CellGeneticData[rowCount][colCount];
    int[][] cellGroups = new int[rowCount][colCount];

    int[] data = ArrayUtil.toIntArray(dataBytes);
    int dataIdx = 0;
    int crossoverDataIdx = 0;
    for (int row = 0; row < rowCount; row++) {
      for (int col = 0; col < colCount; col++) {
        int[] cellValues = new int[cellValueCount];
        for (int idx = 0; idx < cellValues.length; idx++) {
          cellValues[idx] = data[dataIdx++];
        }
        cellData[row][col] = new CellGeneticData(cellValues);
        cellGroups[row][col] = crossoverData[crossoverDataIdx];
        crossoverDataIdx += cellValueCount * BYTES_PER_INT;
      }
    }
    return new CellularAutomatonGeneticData(cellData, cellGroups);
  }
}
