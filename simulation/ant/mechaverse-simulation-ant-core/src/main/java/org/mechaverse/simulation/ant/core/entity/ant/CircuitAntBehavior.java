package org.mechaverse.simulation.ant.core.entity.ant;

import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.mechaverse.simulation.common.genetic.CircuitGeneticDataGenerator;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.util.ArrayUtil;

import com.google.common.base.Preconditions;

/**
 * A {@link AntBehavior} implementation that is based on a simulated circuit.
 */
public class CircuitAntBehavior implements AntBehavior {

  public static final String CIRCUIT_STATE_KEY = "circuitState";
  public static final String CIRCUIT_OUTPUT_MAP_KEY = "circuitOutputMap";
  public static final String CIRCUIT_BIT_OUTPUT_MAP_KEY = "circuitBitOutputMap";

  private final int circuitIndex;
  private final int[] antOutputData;
  private final int[] circuitOutputData;
  private int[] bitOutputMap;
  private final AntOutput output = new AntOutput();
  private final int[] circuitState;
  private SimulationDataStore state = new SimulationDataStore();
  private boolean stateSet = false;
  private final CircuitSimulator circuitSimulator;
  private CircuitGeneticDataGenerator geneticDataGenerator = new CircuitGeneticDataGenerator();

  public CircuitAntBehavior(CircuitSimulator circuitSimulator) {
    this.circuitSimulator = circuitSimulator;

    this.antOutputData = new int[AntOutput.DATA_SIZE];
    this.circuitOutputData = new int[circuitSimulator.getCircuitOutputSize()];
    this.bitOutputMap = new int[circuitSimulator.getCircuitOutputSize()];
    this.circuitIndex = circuitSimulator.getAllocator().allocateCircuit();
    this.circuitState = new int[circuitSimulator.getCircuitStateSize()];
  }

  @Override
  public void setInput(AntInput input, RandomGenerator random) {
    if (!stateSet) {
      GeneticDataStore geneticData;
      if (state.containsKey(GeneticDataStore.KEY)) {
        geneticData = loadGeneticData();
      } else {
        geneticData = generateGeneticData(random);
      }

      initializeCircuit(geneticData);
      stateSet = true;
    }

    circuitSimulator.setCircuitInput(circuitIndex, input.getData());
  }

  @Override
  public AntOutput getOutput(RandomGenerator random) {
    updateAntOutputData();
    output.setData(antOutputData);
    return output;
  }

  @Override
  public void onRemoveEntity() {
    circuitSimulator.getAllocator().deallocateCircuit(circuitIndex);
    stateSet = false;
  }

  @Override
  public void setState(SimulationDataStore state) {
    this.state = state;

    // If the ant has an existing circuit state then load it.
    byte[] circuitStateBytes = state.get(CIRCUIT_STATE_KEY);
    if (circuitStateBytes != null) {
      circuitSimulator.setCircuitState(circuitIndex, ArrayUtil.toIntArray(circuitStateBytes));
      stateSet = true;
    }

    byte[] outputMapBytes = state.get(CIRCUIT_OUTPUT_MAP_KEY);
    if (outputMapBytes != null) {
      circuitSimulator.setCircuitOutputMap(circuitIndex, ArrayUtil.toIntArray(outputMapBytes));
    }

    byte[] bitOutputMapBytes = state.get(CIRCUIT_BIT_OUTPUT_MAP_KEY);
    if (bitOutputMapBytes != null) {
      this.bitOutputMap = ArrayUtil.toIntArray(bitOutputMapBytes);
    }
  }

  @Override
  public SimulationDataStore getState() {
    Preconditions.checkState(stateSet, "State is not set");

    circuitSimulator.getCircuitState(circuitIndex, circuitState);
    state.put(CIRCUIT_STATE_KEY, ArrayUtil.toByteArray(circuitState));
    return state;
  }

  private GeneticDataStore generateGeneticData(RandomGenerator random) {
    try {
      GeneticDataStore geneticData = geneticDataGenerator.generateGeneticData(
          circuitSimulator.getCircuitStateSize(), circuitSimulator.getCircuitOutputSize(), random);
      GeneticData bitOutputMapData = geneticDataGenerator.generateOutputMapGeneticData(
          circuitSimulator.getCircuitOutputSize(), 32, random);
      geneticData.put(CIRCUIT_BIT_OUTPUT_MAP_KEY, bitOutputMapData);

      state.put(GeneticDataStore.KEY, geneticData.serialize());

      return geneticData;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private GeneticDataStore loadGeneticData() {
    try {
      return GeneticDataStore.deserialize(state.get(GeneticDataStore.KEY));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void initializeCircuit(GeneticDataStore geneticData) {
    // Circuit state.
    int[] circuitState = geneticDataGenerator.getCircuitState(geneticData);
    circuitSimulator.setCircuitState(circuitIndex, circuitState);
    state.put(CIRCUIT_STATE_KEY, ArrayUtil.toByteArray(circuitState));

    // Circuit output map.
    int[] outputMap = geneticDataGenerator.getOutputMap(geneticData);
    circuitSimulator.setCircuitOutputMap(circuitIndex, outputMap);
    state.put(CIRCUIT_OUTPUT_MAP_KEY, ArrayUtil.toByteArray(outputMap));

    // Bit output map.
    byte[] bitOutputMapBytes = geneticData.get(CIRCUIT_BIT_OUTPUT_MAP_KEY).getData();
    this.bitOutputMap = ArrayUtil.toIntArray(bitOutputMapBytes);
    state.put(CIRCUIT_BIT_OUTPUT_MAP_KEY, bitOutputMapBytes);
  }

  private void updateAntOutputData() {
    circuitSimulator.getCircuitOutput(circuitIndex, circuitOutputData);
    int antOutputIdx = -1;
    for (int idx = 0; idx < circuitOutputData.length; idx++) {
      int bitPosition = idx % Integer.SIZE;
      if (bitPosition == 0) {
        antOutputIdx++;
        antOutputData[antOutputIdx] = 0;
      }

      // Isolate the selected bit of the circuit output value and or it into the ant output data.
      antOutputData[antOutputIdx] |=
          ((circuitOutputData[idx] >> bitOutputMap[idx]) & 0b1) << bitPosition;
    }
  }
}
