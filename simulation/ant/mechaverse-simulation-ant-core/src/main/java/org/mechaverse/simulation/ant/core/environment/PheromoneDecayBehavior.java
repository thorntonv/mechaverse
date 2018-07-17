package org.mechaverse.simulation.ant.core.environment;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.model.Cell;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.common.EntityManager;
import org.springframework.beans.factory.annotation.Value;

/**
 * A module that periodically decays pheromones. The granularity of this implementation is more
 * coarse than decaying each pheromone every update, but its performance is significantly better.
 */
public class PheromoneDecayBehavior extends AbstractAntEnvironmentBehavior {

  @Value("#{properties['pheromoneDecayInterval']}") private int decayInterval;

  @Override
  public void beforeUpdate(AntSimulationModel model, CellEnvironment env,
      EntityManager<AntSimulationModel, EntityModel<EntityType>> entityManager, RandomGenerator random) {
    if (model.getIteration() % decayInterval == 0) {
      decayPheromones(decayInterval, env, entityManager);
    }
  }

  /**
   * Decays all pheromones by the specified amount. Pheromones that are fully decayed will be
   * removed.
   */
  public void decayPheromones(int decayAmount, CellEnvironment env,
      EntityManager<AntSimulationModel, EntityModel<EntityType>> entityManager) {
    for (int row = 0; row < env.getHeight(); row++) {
      for (int col = 0; col < env.getWidth(); col++) {
        Cell cell = env.getCell(row, col);
        EntityModel<EntityType> pheromone = cell.getEntity(EntityType.PHEROMONE);
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
