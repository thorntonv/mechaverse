package org.mechaverse.simulation.ant.core.entity.ant;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.common.circuit.CircuitDataSource;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.genetic.CircuitGeneticDataGenerator;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link AntBehavior} implementation that is based on a simulated circuit.
 */
public class CircuitAntBehavior implements AntBehavior {

  public static final String CIRCUIT_STATE_KEY = "circuitState";
  public static final String CIRCUIT_OUTPUT_MAP_KEY = "circuitOutputMap";
  public static final String CIRCUIT_BIT_OUTPUT_MAP_KEY = "circuitBitOutputMap";

  private static final Logger logger = LoggerFactory.getLogger(CircuitAntBehavior.class);

  private Ant entity;
  private final int circuitIndex;
  private final int[] antOutputData;
  private final int[] circuitOutputData;
  private int[] bitOutputMap;
  private final AntOutput output = new AntOutput();
  private final int[] circuitState;
  private SimulationDataStore dataStore;
  private GeneticDataStore geneticDataStore;
  private boolean stateSet = false;
  private final CircuitSimulationModel circuitModel;
  private final CircuitSimulator circuitSimulator;
  private CircuitGeneticDataGenerator geneticDataGenerator = new CircuitGeneticDataGenerator();

  public CircuitAntBehavior(
      CircuitDataSource circuitDataSource, CircuitSimulator circuitSimulator) {
    this.circuitModel = circuitDataSource.getCircuitSimulationModel();
    this.circuitSimulator = circuitSimulator;

    this.antOutputData = new int[AntOutput.DATA_SIZE];
    this.circuitOutputData = new int[circuitSimulator.getCircuitOutputSize()];
    this.bitOutputMap = new int[circuitSimulator.getCircuitOutputSize()];
    this.circuitIndex = circuitSimulator.getAllocator().allocateCircuit();
    this.circuitState = new int[circuitSimulator.getCircuitStateSize()];
  }

  @Override
  public void setEntity(Ant entity) {
    this.entity = entity;
  }

  @Override
  public void setInput(AntInput input, RandomGenerator random) {
    if (!stateSet) {
      if (geneticDataStore.size() == 0) {
        generateGeneticData(random);
      }

      initializeCircuit();
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

    // Remove entity state.
    if (dataStore != null) {
      dataStore.clear();
    }
    if (geneticDataStore != null) {
      geneticDataStore.clear();
    }
  }

  @Override
  public void setState(AntSimulationState state) {
    this.dataStore = state.getEntityDataStore(entity);
    this.geneticDataStore = state.getEntityGeneticDataStore(entity);

    // If the ant has an existing circuit state then load it.
    byte[] circuitStateBytes = dataStore.get(CIRCUIT_STATE_KEY);
    if (circuitStateBytes != null) {
      int[] circuitState = ArrayUtil.toIntArray(circuitStateBytes);
      circuitSimulator.setCircuitState(circuitIndex, circuitState);
      stateSet = true;
      logger.trace("setState {} circuitState = {}", entity.getId(),
          Arrays.hashCode(circuitState));
    }

    byte[] outputMapBytes = dataStore.get(CIRCUIT_OUTPUT_MAP_KEY);
    if (outputMapBytes != null) {
      circuitSimulator.setCircuitOutputMap(circuitIndex, ArrayUtil.toIntArray(outputMapBytes));
    }

    byte[] bitOutputMapBytes = dataStore.get(CIRCUIT_BIT_OUTPUT_MAP_KEY);
    if (bitOutputMapBytes != null) {
      this.bitOutputMap = ArrayUtil.toIntArray(bitOutputMapBytes);
    }
  }

  @Override
  public void updateState(AntSimulationState state) {
    if(stateSet) {
      circuitSimulator.getCircuitState(circuitIndex, circuitState);
      state.getEntityDataStore(entity).put(CIRCUIT_STATE_KEY, ArrayUtil.toByteArray(circuitState));
      logger.trace("updateState {} circuitState = {}", entity.getId(),
          Arrays.hashCode(circuitState));
    }
  }

  private void generateGeneticData(RandomGenerator random) {
    geneticDataGenerator.generateGeneticData(geneticDataStore, circuitModel,
        circuitSimulator.getCircuitOutputSize(), random);
    GeneticData bitOutputMapData = geneticDataGenerator.generateOutputMapGeneticData(
        circuitSimulator.getCircuitOutputSize(), 32, random);
    geneticDataStore.put(CIRCUIT_BIT_OUTPUT_MAP_KEY, bitOutputMapData);
  }

  private void initializeCircuit() {
    // Circuit state.
    int[] circuitState = geneticDataGenerator.getCircuitState(geneticDataStore);
    circuitSimulator.setCircuitState(circuitIndex, circuitState);
    dataStore.put(CIRCUIT_STATE_KEY, ArrayUtil.toByteArray(circuitState));
    logger.trace("initializeCircuit {} circuitState = {}", entity.getId(),
        Arrays.hashCode(circuitState));

    // Circuit output map.
    int[] outputMap = geneticDataGenerator.getOutputMap(geneticDataStore);
    circuitSimulator.setCircuitOutputMap(circuitIndex, outputMap);
    dataStore.put(CIRCUIT_OUTPUT_MAP_KEY, ArrayUtil.toByteArray(outputMap));

    // Bit output map.
    byte[] bitOutputMapBytes = geneticDataStore.get(CIRCUIT_BIT_OUTPUT_MAP_KEY).getData();
    this.bitOutputMap = ArrayUtil.toIntArray(bitOutputMapBytes);
    dataStore.put(CIRCUIT_BIT_OUTPUT_MAP_KEY, bitOutputMapBytes);
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
