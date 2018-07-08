package org.mechaverse.simulation.primordial.core.module;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationState;
import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.primordial.core.CellEnvironment;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An environment module that updates simulated cellular automata before active entity actions are
 * performed.
 */
public class CellularAutomatonSimulationModule implements PrimordialSimulationModule {

  @Autowired private ObjectFactory<CellularAutomatonSimulator> simulatorFactory;
  private CellularAutomatonSimulator simulator;

  @Override
  public void setState(PrimordialSimulationState state, CellEnvironment env, EntityManager entityManager) {
  }

  @Override
  public void updateState(PrimordialSimulationState state, CellEnvironment env,
      EntityManager entityManager) {}

  @Override
  public void onAddEntity(Entity entity, PrimordialSimulationState state) {}

  @Override
  public void onRemoveEntity(Entity entity, PrimordialSimulationState state) {}

  @Override
  public void beforeUpdate(PrimordialSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}

  @Override
  public void beforePerformAction(PrimordialSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {
    if (simulator == null) {
      // Lazily load the cellular automaton simulator.
      simulator = simulatorFactory.getObject();
    }
    simulator.update();
  }

  @Override
  public void afterUpdate(PrimordialSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}
}
