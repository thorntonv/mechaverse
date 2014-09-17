package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Food;
import org.mechaverse.simulation.ant.api.util.EntityUtil;
import org.mechaverse.simulation.common.cellautomata.AbstractProbabilisticEnvironmentGenerator.EntityDistribution;
import org.mechaverse.simulation.common.cellautomata.AbstractProbabilisticEnvironmentGenerator.ProbabilisticLocalGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

/**
 * Generates clusters of food to maintain a minimum quantity of food.
 */
public class FoodGenerator implements EnvironmentSimulationModule {

  /**
   * {@link ProbabilisticLocalGenerator} that generates a cluster of food.
   */
  public static class FoodLocalGenerator extends ProbabilisticLocalGenerator<EntityType> {

    private FoodLocalGenerator(double probability,
        Table<Integer, Integer, EntityDistribution<EntityType>> entityDistributions) {
      super(probability, entityDistributions);
    }

    public static FoodLocalGenerator newInstance(double probability, int radius) {
      return new FoodLocalGenerator(probability, createDistributionMatrix(radius));
    }

    private static ImmutableTable<Integer, Integer, EntityDistribution<EntityType>>
        createDistributionMatrix(int radius) {
      ImmutableTable.Builder<Integer, Integer, EntityDistribution<EntityType>> builder =
          ImmutableTable.builder();

      for (int row = 0; row < radius * 2; row++) {
        for (int col = 0; col < radius * 2; col++) {
          double x = col - radius;
          double y = row - radius;
          double p = (radius - Math.sqrt(x * x + y * y)) / radius;
          builder.put(row, col, EntityDistribution.of(EntityType.FOOD, p > 0 ? p : 0));
        }
      }
      return builder.build();
    }
  }

  private static Logger logger = LoggerFactory.getLogger(FoodGenerator.class);

  private int foodCount = 0;

  @Override
  public void update(final AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {
    if (foodCount < state.getConfig().getMinFoodCount()) {
      int row = random.nextInt(env.getRowCount());
      int col = random.nextInt(env.getColumnCount());

      logger.debug("Generating food at ({}, {})", row, col);

      Function<EntityType, Entity> entityFactory = new Function<EntityType, Entity>() {
        @Override
        public Entity apply(EntityType entityType) {
          Entity entity = EntityUtil.newEntity(entityType);
          if (entityType == EntityType.FOOD) {
            entity.setEnergy(state.getConfig().getFoodInitialEnergy());
            entity.setMaxEnergy(entity.getEnergy());
          }
          return entity;
        }
      };
      new AntSimulationEnvironmentGenerator(entityFactory, entityManager)
          .apply(FoodLocalGenerator.newInstance(1, state.getConfig().getFoodClusterRadius()),
              env, row, col, random);
    }
  }

  @Override
  public void onAddEntity(Entity entity) {
    if (entity instanceof Food) {
      foodCount++;
    }
  }

  @Override
  public void onRemoveEntity(Entity entity) {
    if (entity instanceof Food) {
      foodCount--;
    }
  }
}
