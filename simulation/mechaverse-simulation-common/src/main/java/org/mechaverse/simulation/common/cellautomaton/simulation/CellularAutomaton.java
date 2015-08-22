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

    int getParam(String name);
    void setParam(String name, int value);
    Collection<String> getParamNames();
    
    int getOutputParam(String name, int outputIndex);
    void setOutputParam(String name, int outputIndex, int value);
    Collection<String> getOutputParamNames(int outputIndex);
  }

  int getWidth();
  int getHeight();
  Cell getCell(int row, int column);
  
  void update();
}
