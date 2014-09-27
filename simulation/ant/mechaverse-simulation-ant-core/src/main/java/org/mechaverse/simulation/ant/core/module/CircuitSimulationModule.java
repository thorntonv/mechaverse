package org.mechaverse.simulation.ant.core.module;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An environment module that updates simulated circuits before active entity actions are performed.
 */
public class CircuitSimulationModule implements AntSimulationModule {

  @Autowired private ObjectFactory<CircuitSimulator> circuitSimulatorFactory;
  private CircuitSimulator circuitSimulator;

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
    if (circuitSimulator == null) {
      // Lazily load the circuit simulator.
      circuitSimulator = circuitSimulatorFactory.getObject();
    }
    circuitSimulator.update();
  }

  @Override
  public void afterUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}
}
