package org.mechaverse.simulation.common.cellautomaton.simulation.generator.java;

/**
 * An interface for Java cellular automaton simulations.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface JavaCellularAutomatonSimulation {

  public int getStateSize();
  public void getState(int state[]);
  public void setState(int state[]);
  public void setInput(int input[]);
  public void setOutputMap(int outputMap[]);
  public void getOutput(int output[]);
  public void update();
}
