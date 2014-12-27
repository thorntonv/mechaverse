package org.mechaverse.simulation.common.cellautomaton.simulation;

import org.mechaverse.simulation.common.cellautomaton.simulation.AbstractCellularAutomaton.AbstractCell;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;

/**
 * A {@link CellConnector} implementation that connects a cell to its four neighbors in the von
 * Neumann neighborhood.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class FourNeighborCellConnector implements CellConnector {

  @Override
  public Cell[] getNeighbors(int row, int col, CellularAutomaton cells) {
    Cell[] neighbors = new AbstractCell[4];
    neighbors[0] = cells.getCell(row, col - 1);
    neighbors[1] = cells.getCell(row - 1, col);
    neighbors[2] = cells.getCell(row, col + 1);
    neighbors[3] = cells.getCell(row + 1, col);
    return neighbors;
  }
}
