package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;

/**
 * An environment module that updates simulated circuits before active entity actions are performed.
 */
public class CircuitSimulationModule implements EnvironmentSimulationModule {

  private final CircuitSimulator circuitSimulator;

  public CircuitSimulationModule(CircuitSimulator circuitSimulator) {
    this.circuitSimulator = circuitSimulator;
  }

  @Override
  public void onAddEntity(Entity entity) {}

  @Override
  public void onRemoveEntity(Entity entity) {}

  @Override
  public void beforeUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}

  @Override
  public void beforePerformAction(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {
    circuitSimulator.update();
  }

  @Override
  public void afterUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}
}
