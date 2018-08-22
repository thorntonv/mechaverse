package org.mechaverse.simulation.primordial.core.environment;

import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_INPUT_MAP_KEY;
import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_OUTPUT_MAP_KEY;

import com.google.common.base.Preconditions;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.cellautomaton.SimulationStateCellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator.CellularAutomatonSimulatorParams;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Function;

/**
 * An environment module that updates simulated cellular automata before active entity actions are
 * performed.
 */
public class CellularAutomatonSimulationBehavior extends PrimordialEnvironmentBehavior {

  @Autowired private Function<CellularAutomatonSimulatorParams, CellularAutomatonSimulator> simulatorFactory;
  private CellularAutomatonSimulator simulator;
  private SimulationStateCellularAutomatonDescriptor descriptorDataSource;

  private boolean ioMapsSet = false;

  @Override
  public void setState(PrimordialSimulationModel state,
          Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment) {
    super.setState(state, environment);
    Preconditions.checkState(state.getEntityMaxCountPerEnvironment() > 0);
    if (simulator == null || descriptorDataSource == null) {
      // Lazily load the cellular automaton simulator.
      descriptorDataSource = new SimulationStateCellularAutomatonDescriptor(state);
      descriptorDataSource.setDefaultDescriptorResourceName("primordial-automaton-descriptor.xml");
      CellularAutomatonSimulatorParams params = new CellularAutomatonSimulatorParams();
      params.numAutomata = state.getEntityMaxCountPerEnvironment();
      params.descriptorDataSource = descriptorDataSource;
      simulator = simulatorFactory.apply(params);

      generateIOMaps(state);
    }
  }

  @Override
  public void beforePerformAction(PrimordialSimulationModel state, Environment environment, RandomGenerator random) {
    if (!ioMapsSet) {
      setIOMaps(state);
    }

    simulator.update();
  }

  public CellularAutomatonDescriptorDataSource getDescriptorDataSource() {
    return descriptorDataSource;
  }

  public CellularAutomatonSimulator getSimulator() {
    return simulator;
  }

  @Override
  public void onClose() throws Exception {
    super.onClose();
    simulator.close();
  }

  private void generateIOMaps(PrimordialSimulationModel state) {
    RandomGenerator random = new Well19937c(0x12345);
    if(!state.dataContainsKey(CELLULAR_AUTOMATON_INPUT_MAP_KEY)) {
      int[] inputMap = new int[simulator.getAutomatonInputMapSize()];
      for (int idx = 0; idx < inputMap.length; idx++) {
        inputMap[idx] = random
            .nextInt(descriptorDataSource.getSimulationModel().getCellOutputStateSize());
      }
      state.putData(CELLULAR_AUTOMATON_INPUT_MAP_KEY, ArrayUtil.toByteArray(inputMap));
    }

    if(!state.dataContainsKey(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY)) {
      int[] outputMap = new int[simulator.getAutomatonOutputMapSize()];
      for (int idx = 0; idx < outputMap.length; idx++) {
        outputMap[idx] = random
            .nextInt(descriptorDataSource.getSimulationModel().getCellOutputStateSize());
      }
      state.putData(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY, ArrayUtil.toByteArray(outputMap));
    }
  }

  private void setIOMaps(PrimordialSimulationModel state) {
    Preconditions.checkState(state.dataContainsKey(CELLULAR_AUTOMATON_INPUT_MAP_KEY));
    Preconditions.checkState(state.dataContainsKey(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY));

    for (int automatonIdx = 0; automatonIdx < simulator.size(); automatonIdx++) {
      simulator.setAutomatonInputMap(automatonIdx, ArrayUtil.toIntArray(state.getData(CELLULAR_AUTOMATON_INPUT_MAP_KEY)));
      simulator.setAutomatonOutputMap(automatonIdx, ArrayUtil.toIntArray(state.getData(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY)));
    }
    ioMapsSet = true;
  }
}
