package org.mechaverse.simulation.common.cellautomaton;

import com.google.common.base.Preconditions;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY;

/**
 * A base class for {@link org.mechaverse.simulation.common.EntityBehavior} implementations that are
 * based on a simulated cellular automaton.
 */
@SuppressWarnings("WeakerAccess")
public class CellularAutomatonEntityBehavior<
    SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
    ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>> {

  public static final String CELLULAR_AUTOMATON_STATE_KEY = "cellularAutomatonState";
  public static final String CELLULAR_AUTOMATON_OUTPUT_MAP_KEY = "cellularAutomatonOutputMap";

  private static final Logger logger = LoggerFactory
      .getLogger(CellularAutomatonEntityBehavior.class);

  private ENT_MODEL entity;
  private final int automatonIndex;
  private final int[] automatonOutputData;
  private final int[] automatonState;
  private GeneticDataStore geneticDataStore;
  private boolean stateSet = false;
  private final CellularAutomatonSimulationModel model;
  private final CellularAutomatonSimulator simulator;
  private CellularAutomatonGeneticDataGenerator geneticDataGenerator =
      new CellularAutomatonGeneticDataGenerator();

  public CellularAutomatonEntityBehavior(ENT_MODEL entity, int outputDataSize,
      CellularAutomatonDescriptorDataSource dataSource,
      CellularAutomatonSimulator simulator) {
    this.entity = Preconditions.checkNotNull(entity);
    this.model = dataSource.getSimulationModel();
    this.simulator = simulator;
    this.automatonOutputData = new int[simulator.getAutomatonOutputSize()];
    this.automatonIndex = simulator.getAllocator().allocate();
    this.automatonState = new int[simulator.getAutomatonStateSize()];
  }

  public void setEntity(ENT_MODEL entity) {
    this.entity = entity;
  }

  public void setInput(int[] input, RandomGenerator random) {
    if (!stateSet) {
      if (!geneticDataStore.contains(CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY)) {
        generateGeneticData(random);
      }

      initializeCellularAutomaton();
      stateSet = true;
    }

    simulator.setAutomatonInput(automatonIndex, input);
  }

  public int[] getOutput(RandomGenerator random) {
    updateOutputData();
    return automatonOutputData;
  }

  public void onRemoveEntity() {
    simulator.getAllocator().deallocate(automatonIndex);
    stateSet = false;
  }

  public void setState(SIM_MODEL state) {
    this.geneticDataStore = new GeneticDataStore(entity);

    // If the entity has an existing automaton state then load it.
    byte[] stateBytes = entity.getData(CELLULAR_AUTOMATON_STATE_KEY);
    if (stateBytes != null) {
      int[] automatonState = ArrayUtil.toIntArray(stateBytes);
      simulator.setAutomatonState(automatonIndex, automatonState);
      stateSet = true;
      logger.trace("setState {} automatonState = {}", entity.getId(),
          Arrays.hashCode(automatonState));
    }

    byte[] outputMapBytes = entity.getData(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY);
    if (outputMapBytes != null) {
      simulator.setAutomatonOutputMap(automatonIndex, ArrayUtil.toIntArray(outputMapBytes));
    }
  }

  public void updateState(SIM_MODEL state) {
    if (stateSet) {
      simulator.getAutomatonState(automatonIndex, automatonState);
      entity.putData(CELLULAR_AUTOMATON_STATE_KEY, ArrayUtil.toByteArray(automatonState));
      logger.trace("updateState {} automatonState = {}", entity.getId(),
          Arrays.hashCode(automatonState));
    }
  }

  private void generateGeneticData(RandomGenerator random) {
    geneticDataGenerator.generateGeneticData(geneticDataStore, model,
        simulator.getAutomatonOutputSize(), random);
  }

  private void initializeCellularAutomaton() {
    // Cellular automaton state.
    int[] automatonState = geneticDataGenerator.getCellularAutomatonState(geneticDataStore);
    simulator.setAutomatonState(automatonIndex, automatonState);
    entity.putData(CELLULAR_AUTOMATON_STATE_KEY, ArrayUtil.toByteArray(automatonState));
    logger.trace("initializeCellularAutomaton {} automatonState = {}", entity.getId(),
        Arrays.hashCode(automatonState));

    // Cellular automaton output map.
    int[] outputMap = geneticDataGenerator.getOutputMap(geneticDataStore);
    simulator.setAutomatonOutputMap(automatonIndex, outputMap);
    entity.putData(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY, ArrayUtil.toByteArray(outputMap));
  }

  private void updateOutputData() {
    simulator.getAutomatonOutput(automatonIndex, automatonOutputData);
  }
}
