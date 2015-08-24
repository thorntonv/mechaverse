package org.mechaverse.simulation.common.cellautomaton.simulation.generator.java;

/**
 * Base class extended by the generated Java cellular automaton simulation code.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class AbstractJavaCellularAutomatonSimulationImpl
    implements JavaCellularAutomatonSimulation {

  protected final int numLogicalUnits;
  protected final int[] automatonState;
  protected final int[] automatonInputMap;
  protected final int[] automatonInput;
  protected final int[] automatonOutputMap;
  protected final int[] automatonOutput;
  protected final int[] external;
  protected final int automatonInputLength;
  protected final int automatonOutputLength;
  protected final int iterationsPerUpdate;

  public AbstractJavaCellularAutomatonSimulationImpl(int numLogicalUnits, int numExternalCells,
       int stateSize, int inputSize, int outputSize, int iterationsPerUpdate) {
    this.numLogicalUnits = numLogicalUnits;
    this.automatonState = new int[stateSize];
    this.automatonInputMap = new int[inputSize];
    this.automatonInput = new int[inputSize];
    this.automatonOutputMap = new int[outputSize];
    this.automatonOutput = new int[outputSize];
    this.automatonInputLength = inputSize;
    this.automatonOutputLength = outputSize;
    this.external = new int[numExternalCells * numLogicalUnits];
    this.iterationsPerUpdate = iterationsPerUpdate;
  }

  @Override
  public int getStateSize() {
    return automatonState.length;
  }

  public void setInputMap(int[] inputMap) {
    System.arraycopy(inputMap, 0, this.automatonInputMap, 0, this.automatonInputMap.length);
  }

  @Override
  public void setInput(int[] input) {
    System.arraycopy(input, 0, this.automatonInput, 0, this.automatonInput.length);
  }

  @Override
  public void getState(int[] state) {
    System.arraycopy(this.automatonState, 0, state, 0, this.automatonState.length);
  }

  @Override
  public void setState(int[] state) {
    System.arraycopy(state, 0, this.automatonState, 0, this.automatonState.length);
  }

  @Override
  public void setOutputMap(int[] outputMap) {
    System.arraycopy(outputMap, 0, this.automatonOutputMap, 0, this.automatonOutputMap.length);
  }

  @Override
  public void getOutput(int[] output) {
    System.arraycopy(this.automatonOutput, 0, output, 0, this.automatonOutput.length);
  }

  @Override
  public void update() {
    // Copy input to state.
    for (int idx = 0; idx < automatonInputLength; idx++) {
      automatonState[automatonInputMap[idx]] = automatonInput[idx];
    }

    for (int iteration = 0; iteration < iterationsPerUpdate; iteration++) {
      for (int luIndex = 0; luIndex < numLogicalUnits; luIndex++) {
        updateExternalInputs(luIndex);
      }
      for (int luIndex = 0; luIndex < numLogicalUnits; luIndex++) {
        update(luIndex);
      }
    }

    // Copy state values to output.
    for (int idx = 0; idx < automatonOutputLength; idx++) {
      automatonOutput[idx] = automatonState[automatonOutputMap[idx]];
    }
  }

  protected abstract void updateExternalInputs(int luIndex);
  
  protected abstract void update(int luIndex);
}
