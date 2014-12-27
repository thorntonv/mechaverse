package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mechaverse.cellautomaton.model.Cell;
import org.mechaverse.cellautomaton.model.CellType;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.ConnectionInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.Input;

import com.google.common.collect.ImmutableList;

/**
 * Connects the given matrix of cells so that every cell is connected to 8 of its neighbors. Each
 * cell will be connected to its neighbors to the left, right, above and below as well as the four
 * diagonals. External inputs are created for the missing neighbors on the boundary.
 * 
 * <pre>
 * 1 2 3
 * 0 - 4
 * 7 6 5
 * <pre>
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EightNeighborConnectionInfoBuilder extends AbstractConnectionInfoBuilder {

  private final ConnectionInfo connectionInfo;

  public EightNeighborConnectionInfoBuilder(Cell[][] cells, Map<String, CellType> cellTypeMap) {
    super(cells, cellTypeMap);
    this.connectionInfo = buildConnectionInfo();
  }

  @Override
  public ConnectionInfo build() {
    return connectionInfo;
  }

  protected ConnectionInfo buildConnectionInfo() {
    Map<Cell, Input[]> inputMap = new LinkedHashMap<>();
    List<ExternalCell> externalCells = new ArrayList<>();

    int rowCount = cells.length;
    for (int row = 0; row < rowCount; row++) {
      int colCount = cells[row].length;
      for (int col = 0; col < colCount; col++) {
        Cell cell = cells[row][col];
        Input[] inputs = new Input[8];

        // Connect to left cell.
        if (col > 0) {
          inputs[0] = createCellInput(cells[row][col - 1], 4);
        } else {
          inputs[0] = createExternalInput(0, -1, cells[row][colCount - 1], 4, externalCells);
        }
        
        // Connect to the upper left cell.
        if (col > 0 && row > 0) {
          inputs[1] = createCellInput(cells[row - 1][col - 1], 5);
        } else if (col == 0 && row == 0) {
          inputs[1] =
              createExternalInput(-1, -1, cells[rowCount - 1][colCount - 1], 5, externalCells);
        } else if (row == 0) {
          inputs[1] =
              createExternalInput(-1, 0, cells[rowCount - 1][col - 1], 5, externalCells);
        } else {
          // col == 0
          inputs[1] =
              createExternalInput(0, -1, cells[row - 1][colCount - 1], 5, externalCells);
        }
              
        // Connect to the row above.
        if (row > 0) {
          inputs[2] = createCellInput(cells[row - 1][col], 6);
        } else {
          inputs[2] = createExternalInput(-1, 0, cells[rowCount - 1][col], 6, externalCells);
        }
        
        // Connect to the upper right cell.
        if (col < cells[row].length - 1 && row > 0) {
          inputs[3] = createCellInput(cells[row - 1][col + 1], 7);
        } else if (col == cells[row].length - 1 && row == 0) {
          inputs[3] = createExternalInput(-1, 1, cells[rowCount - 1][0], 7, externalCells);
        } else if (col == cells[row].length - 1) {
          inputs[3] = createExternalInput(0, 1, cells[row - 1][0], 7, externalCells);
        } else {
          // row == 0
          inputs[3] = createExternalInput(-1, 0, cells[rowCount - 1][col + 1], 7, externalCells);
        }
        
        // Connect to the right cell.
        if (col < cells[row].length - 1) {
          inputs[4] = createCellInput(cells[row][col + 1], 0);
        } else {
          inputs[4] = createExternalInput(0, 1, cells[row][0], 0, externalCells);
        }
        
        // Connect to the lower right cell.
        if (col < cells[row].length - 1 && row < cells.length - 1) {
          inputs[5] = createCellInput(cells[row + 1][col + 1], 1);
        } else if (col == cells[row].length - 1 && row == cells.length - 1) {
          inputs[5] = createExternalInput(1, 1, cells[0][0], 1, externalCells);
        } else if (col == cells[row].length - 1) {
          inputs[5] = createExternalInput(0, 1, cells[row + 1][0], 1, externalCells);
        } else {
          // row == cells.length - 1
          inputs[5] = createExternalInput(1, 0, cells[0][col + 1], 1, externalCells);
        }
                
        // Connect to the row below.
        if (row < cells.length - 1) {
          inputs[6] = createCellInput(cells[row + 1][col], 2);
        } else {
          inputs[6] = createExternalInput(1, 0, cells[0][col], 2, externalCells);
        }

        // Connect to the lower left cell.
        if (col > 0 && row < cells.length - 1) {
          inputs[7] = createCellInput(cells[row + 1][col - 1], 3);
        } else if (col == 0 && row == cells.length - 1) {
          inputs[7] = createExternalInput(1, -1, cells[0][colCount - 1], 3, externalCells);
        } else if (col == 0) {
          inputs[7] = createExternalInput(0, -1, cells[row + 1][colCount - 1], 3, externalCells);
        } else {
          inputs[7] = createExternalInput(1, 0, cells[0][col - 1], 3, externalCells);
        }

        inputMap.put(cell, inputs);
      }
    }

    return new ConnectionInfo(inputMap, ImmutableList.copyOf(externalCells));
  }
}
