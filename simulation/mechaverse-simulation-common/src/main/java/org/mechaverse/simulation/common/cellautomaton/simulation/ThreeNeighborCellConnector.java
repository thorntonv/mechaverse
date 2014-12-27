package org.mechaverse.simulation.common.cellautomaton.simulation;

import org.mechaverse.simulation.common.cellautomaton.simulation.AbstractCellularAutomaton.AbstractCell;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;

/**
 * Connects the given matrix of cells so that every cell is connected to 3 of its neighbors. Each
 * cell will be connected to its left and right neighbors and alternating cells will be connected to
 * the neighbors above and below.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class ThreeNeighborCellConnector implements CellConnector {

  @Override
  public Cell[] getNeighbors(int row, int col, CellularAutomaton cells) {
    Cell[] neighbors = new AbstractCell[4];
    neighbors[0] = cells.getCell(row, col - 1);
    if (col % 2 == row % 2) {
      neighbors[1] = cells.getCell(row - 1, col);
    } else {
      neighbors[1] = cells.getCell(row + 1, col);
    }
    neighbors[2] = cells.getCell(row, col + 1);
    return neighbors;
  }
}
