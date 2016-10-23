package org.mechaverse.simulation.ant.core.module;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.AntSimulationState;
import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.Cell;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;
import org.springframework.beans.factory.annotation.Value;

/**
 * A module that periodically decays pheromones. The granularity of this implementation is more
 * coarse than decaying each pheromone every update, but its performance is significantly better.
 */
public class PheromoneDecayModule implements AntSimulationModule {

  @Value("#{properties['pheromoneDecayInterval']}") private int decayInterval;

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
      EntityManager entityManager, RandomGenerator random) {
    if (state.getModel().getIteration() % decayInterval == 0) {
      decayPheromones(decayInterval, env, entityManager);
    }
  }

  @Override
  public void beforePerformAction(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}

  @Override
  public void afterUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}

  /**
   * Decays all pheromones by the specified amount. Pheromones that are fully decayed will be
   * removed.
   */
  public void decayPheromones(int decayAmount, CellEnvironment env, EntityManager entityManager) {
    for (int row = 0; row < env.getRowCount(); row++) {
      for (int col = 0; col < env.getColumnCount(); col++) {
        Cell cell = env.getCell(row, col);
        Entity pheromone = cell.getEntity(EntityType.PHEROMONE);
        if (pheromone != null) {
          if (pheromone.getEnergy() > decayAmount) {
            pheromone.setEnergy(pheromone.getEnergy() - decayAmount);
          } else {
            entityManager.removeEntity(pheromone);
          }
        }
      }
    }
  }
}
