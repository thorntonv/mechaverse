package org.mechaverse.simulation.common.circuit.generator.java;

/**
 * Base class extended by the generated Java circuit simulation code.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class AbstractJavaCircuitSimulationImpl implements JavaCircuitSimulation {

  protected final int numLogicalUnits;
  protected final int[] circuitState;
  protected final int[] circuitInput;
  protected final int[] circuitOutputMap;
  protected final int[] circuitOutput;
  protected final int[] external;
  protected final int circuitInputLength;
  protected final int circuitOutputLength;
  protected final int iterationsPerUpdate;

  public AbstractJavaCircuitSimulationImpl(int numLogicalUnits, int numExternalElements,
       int stateSize, int inputSize, int outputSize, int iterationsPerUpdate) {
    this.numLogicalUnits = numLogicalUnits;
    this.circuitState = new int[stateSize];
    this.circuitInput = new int[inputSize];
    this.circuitOutputMap = new int[outputSize];
    this.circuitOutput = new int[outputSize];
    this.circuitInputLength = inputSize;
    this.circuitOutputLength = outputSize;
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
  public void setOutputMap(int[] outputMap) {
    System.arraycopy(outputMap, 0, this.circuitOutputMap, 0, this.circuitOutputMap.length);
  }

  @Override
  public void getOutput(int[] output) {
    System.arraycopy(this.circuitOutput, 0, output, 0, this.circuitOutput.length);
  }

  @Override
  public void update() {
    for (int iteration = 0; iteration < iterationsPerUpdate; iteration++) {
      for (int luIndex = 0; luIndex < numLogicalUnits; luIndex++) {
        updateExternalInputs(luIndex);
      }
      for (int luIndex = 0; luIndex < numLogicalUnits; luIndex++) {
        update(luIndex);
      }
    }

    // Copy state values to output.
    for (int idx = 0; idx < circuitOutputLength; idx++) {
      circuitOutput[idx] = circuitState[circuitOutputMap[idx]];
    }
  }

  protected abstract void updateExternalInputs(int luIndex);
  
  protected abstract void update(int luIndex);
}
