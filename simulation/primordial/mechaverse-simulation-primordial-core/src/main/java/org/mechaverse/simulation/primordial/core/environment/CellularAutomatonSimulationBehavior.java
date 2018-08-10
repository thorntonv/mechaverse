package org.mechaverse.simulation.primordial.core.environment;

import com.google.common.base.Preconditions;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.cellautomaton.SimulationStateCellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator.CellularAutomatonSimulatorParams;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An environment module that updates simulated cellular automata before active entity actions are
 * performed.
 */
public class CellularAutomatonSimulationBehavior extends PrimordialEnvironmentBehavior {

  @Autowired private Function<CellularAutomatonSimulatorParams, CellularAutomatonSimulator> simulatorFactory;
  private CellularAutomatonSimulator simulator;
  private SimulationStateCellularAutomatonDescriptor descriptorDataSource;

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
    }
  }

  @Override
  public void beforePerformAction(PrimordialSimulationModel state, Environment environment, RandomGenerator random) {
    simulator.update();
  }

  public CellularAutomatonDescriptorDataSource getDescriptorDataSource() {
    return descriptorDataSource;
  }

  public CellularAutomatonSimulator getSimulator() {
    return simulator;
  }}
