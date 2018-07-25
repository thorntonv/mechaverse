package org.mechaverse.simulation.primordial.core.environment;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An environment module that updates simulated cellular automata before active entity actions are
 * performed.
 */
public class CellularAutomatonSimulationModule extends PrimordialEnvironmentBehavior {

  @Autowired
  private ObjectFactory<CellularAutomatonSimulator> simulatorFactory;
  private CellularAutomatonSimulator simulator;

  @Override
  public void beforePerformAction(PrimordialSimulationModel state,
      Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> env,
          RandomGenerator random) {
    if (simulator == null) {
      // Lazily load the cellular automaton simulator.
      simulator = simulatorFactory.getObject();
    }
    simulator.update();
  }
}
