package org.mechaverse.simulation.common.circuit.generator.java;

/**
 * Base class extended by the generated Java circuit simulation code.
 *
 * @author thorntonv@mechaverse.org
 */
public abstract class AbstractJavaCircuitSimulationImpl implements JavaCircuitSimulation {

  protected final int numLogicalUnits;
  protected final int[] circuitState;
  protected final int[] circuitInput;
  protected final int[] external;
  protected final int circuitInputLength;
  protected final int iterationsPerUpdate;

  public AbstractJavaCircuitSimulationImpl(int numLogicalUnits, int numExternalElements,
       int stateSize, int inputSize, int iterationsPerUpdate) {
    this.numLogicalUnits = numLogicalUnits;
    this.circuitState = new int[stateSize];
    this.circuitInput = new int[inputSize];
    this.circuitInputLength = inputSize;
    this.external = new int[numExternalElements * numLogicalUnits];
    this.iterationsPerUpdate = iterationsPerUpdate;
  }

  @Override
  public int getStateSize() {
    return circuitState.length;
  }

  @Override
  public void setInput(int[] input) {
    System.arraycopy(input, 0, this.circuitInput, 0, this.circuitInput.length);
  }

  @Override
  public void getState(int[] state) {
    System.arraycopy(this.circuitState, 0, state, 0, this.circuitState.length);
  }

  @Override
  public void setState(int[] state) {
    System.arraycopy(state, 0, this.circuitState, 0, this.circuitState.length);
  }

  @Override
  public void update() {
    for (int iteration = 0; iteration < iterationsPerUpdate; iteration++) {
      for (int idx = 0; idx < external.length; idx++) {
        external[idx] = circuitState[idx];
      }
      for (int luIndex = 0; luIndex < numLogicalUnits; luIndex++) {
        update(luIndex);
      }
    }
  }

  protected abstract void update(int luIndex);
}
