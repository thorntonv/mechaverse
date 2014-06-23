package org.mechaverse.simulation.common.circuit.generator.java;

/**
 * Base class extended by the generated Java circuit simulation code.
 *
 * @author thorntonv@mechaverse.org
 */
public abstract class AbstractJavaCircuitSimulationImpl implements JavaCircuitSimulation {

  protected final int numLogicalUnits;
  protected final int[] state;

  public AbstractJavaCircuitSimulationImpl(int numLogicalUnits, int stateSize) {
    this.numLogicalUnits = numLogicalUnits;
    this.state = new int[stateSize];
  }

  @Override
  public int getStateSize() {
    return state.length;
  }

  @Override
  public void getState(int[] state) {
    System.arraycopy(this.state, 0, state, 0, this.state.length);
  }

  @Override
  public void setState(int[] state) {
    System.arraycopy(state, 0, this.state, 0, this.state.length);
  }

  @Override
  public void update() {
    for (int luIndex = 0; luIndex < numLogicalUnits; luIndex++) {
      update(luIndex);
    }
  }

  protected abstract void update(int luIndex);
}
