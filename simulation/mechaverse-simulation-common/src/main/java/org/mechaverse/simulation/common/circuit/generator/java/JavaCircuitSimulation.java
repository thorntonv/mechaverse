package org.mechaverse.simulation.common.circuit.generator.java;

/**
 * An interface for Java circuit simulations.
 *
 * @author thorntonv@mechaverse.org
 */
public interface JavaCircuitSimulation {

  public int getStateSize();
  public void getState(int state[]);
  public void setState(int state[]);
  public void update();
}
