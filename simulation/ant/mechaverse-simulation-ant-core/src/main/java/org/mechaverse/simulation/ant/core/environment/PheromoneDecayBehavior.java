package org.mechaverse.simulation.ant.core.environment;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.Cell;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.model.EntityModel;

/**
 * A module that periodically decays pheromones. The granularity of this implementation is more
 * coarse than decaying each pheromone every update, but its performance is significantly better.
 */
public class PheromoneDecayBehavior extends AbstractAntEnvironmentBehavior {

  private int decayInterval;

  @Override
  public void setState(AntSimulationModel state,
      Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> environment) {
    super.setState(state, environment);
    decayInterval = state.getPheromoneDecayInterval();
  }

  @Override
  public void beforeUpdate(AntSimulationModel model,
      Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> env, RandomGenerator random) {
    if (model.getIteration() % decayInterval == 0) {
      decayPheromones(decayInterval, env);
    }
  }

  /**
   * Decays all pheromones by the specified amount. Pheromones that are fully decayed will be
   * removed.
   */
  public void decayPheromones(int decayAmount,
          Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> env) {
    CellEnvironment envModel = env.getModel();
    for (int row = 0; row < envModel.getHeight(); row++) {
      for (int col = 0; col < envModel.getWidth(); col++) {
        Cell cell = envModel.getCell(row, col);
        EntityModel<EntityType> pheromone = cell.getEntity(EntityType.PHEROMONE);
        if (pheromone != null) {
          if (pheromone.getEnergy() > decayAmount) {
            pheromone.setEnergy(pheromone.getEnergy() - decayAmount);
          } else {
            env.removeEntity(pheromone);
          }
        }
      }
    }
  }
}
