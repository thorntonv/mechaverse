package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;

/**
 * A module that periodically decays pheromones. The granularity of this implementation is more
 * coarse than decaying each pheromone every update, but its performance is significantly better.
 */
public class PheromoneDecayModule implements EnvironmentSimulationModule {

  private int decayInterval = 60 * 60;
  private int updateCount = 0;

  @Override
  public void onAddEntity(Entity entity) {}

  @Override
  public void onRemoveEntity(Entity entity) {}

  @Override
  public void update(CellEnvironment env, EntityManager entityManager, RandomGenerator random) {
    updateCount++;
    if (updateCount >= decayInterval) {
      decayPheromones(decayInterval, env, entityManager);
      updateCount = 0;
    }
  }

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
