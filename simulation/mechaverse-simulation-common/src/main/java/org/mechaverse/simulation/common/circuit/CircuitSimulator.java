package org.mechaverse.simulation.common.circuit;

/**
 * An interface for circuit simulators.
 *
 * @author thorntonv@mechaverse.org
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
  public void getCircuitOutput(int circuitIndex, int[] circuitOutput);
  public void update();
}
