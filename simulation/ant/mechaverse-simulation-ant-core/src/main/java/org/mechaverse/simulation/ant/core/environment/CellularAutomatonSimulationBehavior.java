package org.mechaverse.simulation.ant.core.environment;

import com.google.common.base.Preconditions;
import java.util.function.Function;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.cellautomaton.SimulationStateCellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator.CellularAutomatonSimulatorParams;
import org.mechaverse.simulation.common.model.EntityModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An environment module that updates simulated cellular automata before active entity actions are
 * performed.
 */
public class CellularAutomatonSimulationBehavior extends AbstractAntEnvironmentBehavior {

  @Autowired private Function<CellularAutomatonSimulatorParams, CellularAutomatonSimulator> simulatorFactory;
  private CellularAutomatonSimulator simulator;
  private CellularAutomatonDescriptorDataSource descriptorDataSource;

  @Override
  public void setState(AntSimulationModel state,
      Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> environment) {
    Preconditions.checkState(state.getAntMaxCount() > 0);
    super.setState(state, environment);
    if (simulator == null || descriptorDataSource == null) {
      // Lazily load the cellular automaton simulator.
      descriptorDataSource = new SimulationStateCellularAutomatonDescriptor(state);
      CellularAutomatonSimulatorParams params = new CellularAutomatonSimulatorParams();
      params.numAutomata = state.getAntMaxCount();
      params.descriptorDataSource = descriptorDataSource;
      simulator = simulatorFactory.apply(params);
    }
  }

  @Override
  public void beforePerformAction(AntSimulationModel state, Environment environment, RandomGenerator random) {
    simulator.update();
  }

  public CellularAutomatonDescriptorDataSource getDescriptorDataSource() {
    return descriptorDataSource;
  }

  public CellularAutomatonSimulator getSimulator() {
    return simulator;
  }
}
