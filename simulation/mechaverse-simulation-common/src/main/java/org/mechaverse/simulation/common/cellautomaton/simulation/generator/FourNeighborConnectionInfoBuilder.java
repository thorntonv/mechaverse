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
 * Connects the given matrix of cells so that every cell is connected to 4 of its neighbors. Each
 * cell will be connected to its left and right neighbors as well as the neighbors above and below.
 * External inputs are created for the missing neighbors on the boundary.
 * 
 * <pre>
 * - 1 -
 * 0 - 2
 * - 3 -
 * </pre>
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class FourNeighborConnectionInfoBuilder extends AbstractConnectionInfoBuilder {

  private final ConnectionInfo connectionInfo;

  public FourNeighborConnectionInfoBuilder(Cell[][] cells, Map<String, CellType> cellTypeMap) {
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
        Input[] inputs = new Input[4];

        // Connect to left cell.
        if (col > 0) {
          inputs[0] = createCellInput(cells[row][col - 1], 2);
        } else {
          inputs[0] = createExternalInput(0, -1, cells[row][colCount - 1], 2, externalCells);
        }
        
        // Connect to the row above.
        if (row > 0) {
          inputs[1] = createCellInput(cells[row - 1][col], 3);
        } else {
          inputs[1] = createExternalInput(-1, 0, cells[rowCount - 1][col], 3, externalCells);
        }
        
        // Connect to the right cell.
        if (col < cells[row].length - 1) {
          inputs[2] = createCellInput(cells[row][col + 1], 0);
        } else {
          inputs[2] = createExternalInput(0, 1, cells[row][0], 0, externalCells);
        }
        
        // Connect to the row below.
        if (row < cells.length - 1) {
          inputs[3] = createCellInput(cells[row + 1][col], 1);
        } else {
          inputs[3] = createExternalInput(1, 0, cells[0][col], 1, externalCells);
        }
        
        inputMap.put(cell, inputs);
      }
    }

    return new ConnectionInfo(inputMap, ImmutableList.copyOf(externalCells));
  }
}
