package org.mechaverse.simulation.ant.core.module;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An environment module that updates simulated cellular automata before active entity actions are
 * performed.
 */
public class CellularAutomatonSimulationModule implements AntSimulationModule {

  @Autowired private ObjectFactory<CellularAutomatonSimulator> simulatorFactory;
  private CellularAutomatonSimulator simulator;

  @Override
  public void setState(AntSimulationState state, CellEnvironment env, EntityManager entityManager) {
  }

  @Override
  public void updateState(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager) {}

  @Override
  public void onAddEntity(Entity entity, AntSimulationState state) {}

  @Override
  public void onRemoveEntity(Entity entity, AntSimulationState state) {}

  @Override
  public void beforeUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}

  @Override
  public void beforePerformAction(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {
    if (simulator == null) {
      // Lazily load the cellular automaton simulator.
      simulator = simulatorFactory.getObject();
    }
    simulator.update();
  }

  @Override
  public void afterUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}
}
