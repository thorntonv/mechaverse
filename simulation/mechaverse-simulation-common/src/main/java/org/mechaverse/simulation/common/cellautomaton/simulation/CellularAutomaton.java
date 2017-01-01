package org.mechaverse.simulation.common.cellautomaton.simulation;

import java.util.Collection;

/**
 * An interface for cellular automatons.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface CellularAutomaton {

  interface Cell {

    int getOutput(int idx);
    void setOutput(int idx, int value);
    int getOutputCount();
  }

  int getWidth();
  int getHeight();
  Cell getCell(int row, int column);
  
  void update();
  void updateInputs();

}
