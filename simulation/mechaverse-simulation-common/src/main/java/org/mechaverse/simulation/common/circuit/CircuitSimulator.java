package org.mechaverse.simulation.common.circuit;

/**
 * An interface for circuit simulators.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface CircuitSimulator extends AutoCloseable {

  public CircuitAllocator getAllocator();
  public int getCircuitCount();
  public int getCircuitInputSize();
  public int getCircuitStateSize();
  public int getCircuitOutputSize();

  public void getCircuitState(int circuitIndex, int[] circuitState);
  public void setCircuitState(int circuitIndex, int[] circuitState);
  public void setCircuitInput(int circuitIndex, int[] circuitInput);

  /**
   * Sets the circuit output map.
   *
   * @param circuitIndex the index of the circuit to set the output map of
   * @param outputMap An array where the value at each index i is the index of the state value that
   *        should be output to output index i.
   */
  public void setCircuitOutputMap(int circuitIndex, int[] outputMap);
  public void getCircuitOutput(int circuitIndex, int[] circuitOutput);
  public void update();
}
