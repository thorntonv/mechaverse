package org.mechaverse.simulation.common.cellautomaton.simulation;

/**
 * An interface for cellular automaton simulators.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface CellularAutomatonSimulator extends AutoCloseable {

  public CellularAutomatonAllocator getAllocator();
  public int size();
  public int getAutomatonInputSize();
  public int getAutomatonStateSize();
  public int getAutomatonOutputSize();
  
  public void getAutomatonState(int index, int[] state);
  public void setAutomatonState(int index, int[] state);
  public void setAutomatonInput(int index, int[] input);

  /**
   * Sets the output map.
   *
   * @param index the index of the automaton to set the output map of
   * @param outputMap An array where the value at each index i is the index of the state value that
   *        should be output to output index i.
   */
  public void setAutomatonOutputMap(int index, int[] outputMap);
  public void getAutomatonOutput(int index, int[] output);
  public void update();
}
