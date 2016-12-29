package org.mechaverse.simulation.primordial.core.entity.primordial;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationState;
import org.mechaverse.simulation.primordial.core.entity.primordial.ActivePrimordialEntity.PrimordialEntityBehavior;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * A {@link PrimordialEntityBehavior} implementation that is based on a simulated cellular automaton.
 */
public class CellularAutomatonPrimordialEntityBehavior implements PrimordialEntityBehavior {

  public static final String AUTOMATON_STATE_KEY = "cellularAutomatonState";
  public static final String AUTOMATON_OUTPUT_MAP_KEY = "cellularAutomatonOutputMap";
  public static final String AUTOMATON_BIT_OUTPUT_MAP_KEY = "cellularAutomatonBitOutputMap";

  private static final Logger logger = LoggerFactory.getLogger(CellularAutomatonPrimordialEntityBehavior.class);

  private PrimordialEntity entity;
  private final int automatonIndex;
  private final int[] entityOutputData;
  private final int[] automatonOutputData;
  private int[] bitOutputMap;
  private final PrimordialEntityOutput output = new PrimordialEntityOutput();
  private final int[] automatonState;
  private SimulationDataStore dataStore;
  private GeneticDataStore geneticDataStore;
  private boolean stateSet = false;
  private final CellularAutomatonSimulationModel model;
  private final CellularAutomatonSimulator simulator;
  private CellularAutomatonGeneticDataGenerator geneticDataGenerator =
      new CellularAutomatonGeneticDataGenerator();

  public CellularAutomatonPrimordialEntityBehavior(
      CellularAutomatonDescriptorDataSource dataSource, CellularAutomatonSimulator simulator) {
    this.model = dataSource.getSimulationModel();
    this.simulator = simulator;

    this.entityOutputData = new int[PrimordialEntityOutput.DATA_SIZE];
    this.automatonOutputData = new int[simulator.getAutomatonOutputSize()];
    this.bitOutputMap = new int[simulator.getAutomatonOutputSize()];
    this.automatonIndex = simulator.getAllocator().allocate();
    this.automatonState = new int[simulator.getAutomatonStateSize()];
  }

  @Override
  public void setEntity(PrimordialEntity entity) {
    this.entity = entity;
  }

  @Override
  public void setInput(PrimordialEntityInput input, RandomGenerator random) {
    if (!stateSet) {
      if (geneticDataStore.size() == 0) {
        generateGeneticData(random);
      }

      initializeCellularAutomaton();
      stateSet = true;
    }

    simulator.setAutomatonInput(automatonIndex, input.getData());
  }

  @Override
  public PrimordialEntityOutput getOutput(RandomGenerator random) {
    updateEntityOutputData();
    output.setData(entityOutputData);
    return output;
  }

  @Override
  public void onRemoveEntity() {
    simulator.getAllocator().deallocate(automatonIndex);
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
  public void setState(PrimordialSimulationState state) {
    this.dataStore = state.getEntityDataStore(entity);
    this.geneticDataStore = state.getEntityGeneticDataStore(entity);

    // If the primordial entity has an existing automaton state then load it.
    byte[] stateBytes = dataStore.get(AUTOMATON_STATE_KEY);
    if (stateBytes != null) {
      int[] automatonState = ArrayUtil.toIntArray(stateBytes);
      simulator.setAutomatonState(automatonIndex, automatonState);
      stateSet = true;
      logger.trace("setState {} automatonState = {}", entity.getId(),
          Arrays.hashCode(automatonState));
    }

    byte[] outputMapBytes = dataStore.get(AUTOMATON_OUTPUT_MAP_KEY);
    if (outputMapBytes != null) {
      simulator.setAutomatonOutputMap(automatonIndex, ArrayUtil.toIntArray(outputMapBytes));
    }

    byte[] bitOutputMapBytes = dataStore.get(AUTOMATON_BIT_OUTPUT_MAP_KEY);
    if (bitOutputMapBytes != null) {
      setBitOutputMap(ArrayUtil.toIntArray(bitOutputMapBytes));
    }
  }

  @Override
  public void updateState(PrimordialSimulationState state) {
    if(stateSet) {
      simulator.getAutomatonState(automatonIndex, automatonState);
      state.getEntityDataStore(entity)
          .put(AUTOMATON_STATE_KEY, ArrayUtil.toByteArray(automatonState));
      logger.trace("updateState {} automatonState = {}", entity.getId(),
          Arrays.hashCode(automatonState));
    }
  }

  private void generateGeneticData(RandomGenerator random) {
    geneticDataGenerator.generateGeneticData(geneticDataStore, model,
        simulator.getAutomatonOutputSize(), random);
    GeneticData bitOutputMapData = geneticDataGenerator.generateOutputMapGeneticData(
        simulator.getAutomatonOutputSize(), 32, random);
    geneticDataStore.put(AUTOMATON_BIT_OUTPUT_MAP_KEY, bitOutputMapData);
  }

  private void initializeCellularAutomaton() {
    // Cellular automaton state.
    int[] automatonState = geneticDataGenerator.getCellularAutomatonState(geneticDataStore);
    simulator.setAutomatonState(automatonIndex, automatonState);
    dataStore.put(AUTOMATON_STATE_KEY, ArrayUtil.toByteArray(automatonState));
    logger.trace("initializeCellularAutomaton {} automatonState = {}", entity.getId(),
        Arrays.hashCode(automatonState));

    // Cellular automaton output map.
    int[] outputMap = geneticDataGenerator.getOutputMap(geneticDataStore);
    simulator.setAutomatonOutputMap(automatonIndex, outputMap);
    dataStore.put(AUTOMATON_OUTPUT_MAP_KEY, ArrayUtil.toByteArray(outputMap));

    // Bit output map.
    byte[] bitOutputMapBytes = geneticDataStore.get(AUTOMATON_BIT_OUTPUT_MAP_KEY).getData();
    setBitOutputMap(ArrayUtil.toIntArray(bitOutputMapBytes));
    dataStore.put(AUTOMATON_BIT_OUTPUT_MAP_KEY, bitOutputMapBytes);
  }

  private void updateEntityOutputData() {
    simulator.getAutomatonOutput(automatonIndex, automatonOutputData);
    int entityOutputIdx = -1;
    for (int idx = 0; idx < automatonOutputData.length; idx++) {
      int bitPosition = idx % Integer.SIZE;
      if (bitPosition == 0) {
        entityOutputIdx++;
        entityOutputData[entityOutputIdx] = 0;
      }

      // Isolate the selected bit of the automaton output value and or it into the primordial output data.
      entityOutputData[entityOutputIdx] |=
          ((automatonOutputData[idx] >> bitOutputMap[idx]) & 0b1) << bitPosition;
    }
  }

  private void setBitOutputMap(int[] bitOutputMap) {
    for (int idx = 0; idx < bitOutputMap.length; idx++) {
      bitOutputMap[idx] = Math.abs(bitOutputMap[idx]) % 32;
    }
    this.bitOutputMap = bitOutputMap;
  }
}
