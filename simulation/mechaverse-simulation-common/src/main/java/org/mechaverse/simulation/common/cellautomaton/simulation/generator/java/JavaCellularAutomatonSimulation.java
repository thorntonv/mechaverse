package org.mechaverse.simulation.common.cellautomaton.simulation.generator.java;

/**
 * An interface for Java cellular automaton simulations.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface JavaCellularAutomatonSimulation {

  int getStateSize();
  void getState(int[] state);
  void setState(int[] state);
  void setInputMap(int[] inputMap);
  void setInput(int[] input);
  void setOutputMap(int[] outputMap);
  void getOutput(int[] output);
  void update();
}
