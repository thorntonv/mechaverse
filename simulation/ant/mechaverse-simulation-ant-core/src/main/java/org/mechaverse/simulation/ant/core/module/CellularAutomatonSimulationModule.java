package org.mechaverse.simulation.ant.core.module;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.AbstractAntEnvironmentBehavior;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An environment module that updates simulated cellular automata before active entity actions are
 * performed.
 */
public class CellularAutomatonSimulationModule extends AbstractAntEnvironmentBehavior {

  @Autowired private ObjectFactory<CellularAutomatonSimulator> simulatorFactory;
  private CellularAutomatonSimulator simulator;

  @Override
  public void beforePerformAction(AntSimulationModel state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {
    if (simulator == null) {
      // Lazily load the cellular automaton simulator.
      simulator = simulatorFactory.getObject();
    }
    simulator.update();
  }
}
