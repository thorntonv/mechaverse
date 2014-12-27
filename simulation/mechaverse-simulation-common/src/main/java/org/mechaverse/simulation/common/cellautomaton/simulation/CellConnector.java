package org.mechaverse.simulation.common.cellautomaton.simulation;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;

/**
 * An interface for obtaining the neighbors of a cell.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface CellConnector {

  Cell[] getNeighbors(int row, int col, CellularAutomaton cells);
}
