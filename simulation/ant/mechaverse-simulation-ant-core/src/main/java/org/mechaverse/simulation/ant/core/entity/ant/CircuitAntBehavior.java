package org.mechaverse.simulation.ant.core.entity.ant;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.mechaverse.simulation.common.util.ArrayUtil;

import com.google.common.base.Preconditions;

/**
 * A {@link AntBehavior} implementation that is based on a simulated circuit.
 */
public class CircuitAntBehavior implements AntBehavior {

  public static final String CIRCUIT_STATE_KEY = "circuitState";

  private final int circuitIndex;
  private final int[] outputData;
  private final AntOutput output = new AntOutput();
  private final int[] circuitState;
  private SimulationDataStore state = new SimulationDataStore();
  private boolean stateSet = false;
  private final CircuitSimulator circuitSimulator;

  public CircuitAntBehavior(CircuitSimulator circuitSimulator) {
    this.circuitSimulator = circuitSimulator;

    this.outputData = new int[circuitSimulator.getCircuitOutputSize()];
    this.circuitIndex = circuitSimulator.getAllocator().allocateCircuit();
    this.circuitState = new int[circuitSimulator.getCircuitStateSize()];
  }

  @Override
  public void setInput(AntInput input, RandomGenerator random) {
    if (!stateSet) {
      // If the state is not set generate a random state.
      for (int idx = 0; idx < circuitState.length; idx++) {
        circuitState[idx] = random.nextInt();
      }
      circuitSimulator.setCircuitState(circuitIndex, circuitState);
      stateSet = true;
    }
    circuitSimulator.setCircuitInput(circuitIndex, input.getData());
  }

  @Override
  public AntOutput getOutput(RandomGenerator random) {
    // TODO(thorntonv): Implement circuit output.
    // circuitSimulator.getCircuitOutput(circuitIndex, outputData);
    // output.setData(outputData);
    return output;
  }

  @Override
  public void onRemoveEntity() {
    circuitSimulator.getAllocator().deallocateCircuit(circuitIndex);
    stateSet = false;
  }

  @Override
  public void setState(SimulationDataStore state) {
    // If the ant has an existing circuit state then load it.
    this.state = state;
    byte[] circuitStateBytes = state.get(CIRCUIT_STATE_KEY);
    if (circuitStateBytes != null) {
      circuitSimulator.setCircuitState(circuitIndex, ArrayUtil.toIntArray(circuitStateBytes));
      stateSet = true;
    }
  }

  @Override
  public SimulationDataStore getState() {
    Preconditions.checkState(stateSet, "State is not set");

    circuitSimulator.getCircuitState(circuitIndex, circuitState);
    state.put(CIRCUIT_STATE_KEY, ArrayUtil.toByteArray(circuitState));
    return state;
  }
}
