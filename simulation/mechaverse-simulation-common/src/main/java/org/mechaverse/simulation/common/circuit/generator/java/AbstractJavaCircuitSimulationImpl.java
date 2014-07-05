package org.mechaverse.simulation.common.circuit.generator.java;

/**
 * Base class extended by the generated Java circuit simulation code.
 *
 * @author thorntonv@mechaverse.org
 */
public abstract class AbstractJavaCircuitSimulationImpl implements JavaCircuitSimulation {

  protected final int numLogicalUnits;
  protected final int[] state;
  protected final int[] external;
  protected final int iterationsPerUpdate;

  public AbstractJavaCircuitSimulationImpl(int numLogicalUnits, int numExternalElements,
       int stateSize, int iterationsPerUpdate) {
    this.numLogicalUnits = numLogicalUnits;
    this.state = new int[stateSize];
    this.external = new int[numExternalElements * numLogicalUnits];
    this.iterationsPerUpdate = iterationsPerUpdate;
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
    for (int iteration = 0; iteration < iterationsPerUpdate; iteration++) {
      for (int idx = 0; idx < external.length; idx++) {
        external[idx] = state[idx];
      }
      for (int luIndex = 0; luIndex < numLogicalUnits; luIndex++) {
        update(luIndex);
      }
    }
  }

  protected abstract void update(int luIndex);
}
