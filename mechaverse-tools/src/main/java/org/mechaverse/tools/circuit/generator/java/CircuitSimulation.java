package org.mechaverse.tools.circuit.generator.java;

/**
 * An interface for classes that support executing circuit simulations.
 *
 * @author thorntonv@mechaverse.org
 */
public interface CircuitSimulation {

  public void update(int logicalUnitIdx, int[] circuitState);
}
