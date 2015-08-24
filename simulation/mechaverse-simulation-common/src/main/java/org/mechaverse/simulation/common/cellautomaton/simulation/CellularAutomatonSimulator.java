package org.mechaverse.simulation.common.cellautomaton.simulation;

/**
 * An interface for cellular automaton simulators.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface CellularAutomatonSimulator extends AutoCloseable {

  CellularAutomatonAllocator getAllocator();
  int size();
  int getAutomatonInputSize();
  int getAutomatonStateSize();
  int getAutomatonOutputSize();
  
  void getAutomatonState(int index, int[] state);
  void setAutomatonState(int index, int[] state);

  /**
   * Sets the input map.
   *
   * @param index the index of the automaton to set the input map of
   * @param inputMap an array where the value at each index i is the index of the state value that
   *        should be set to input index i
   */
  void setAutomatonInputMap(int index, int[] inputMap);
  void setAutomatonInput(int index, int[] input);

  /**
   * Sets the output map.
   *
   * @param index the index of the automaton to set the output map of
   * @param outputMap An array where the value at each index i is the index of the state value that
   *        should be output to output index i.
   */
  void setAutomatonOutputMap(int index, int[] outputMap);
  void getAutomatonOutput(int index, int[] output);
  void update();
}
