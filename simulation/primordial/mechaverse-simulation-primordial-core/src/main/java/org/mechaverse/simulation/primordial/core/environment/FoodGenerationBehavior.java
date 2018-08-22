package org.mechaverse.simulation.primordial.core.environment;

import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.AbstractProbabilisticEnvironmentModelGenerator.EntityDistribution;
import org.mechaverse.simulation.common.AbstractProbabilisticEnvironmentModelGenerator.ProbabilisticLocalGenerator;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.Food;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

/**
 * Generates clusters of food to maintain a minimum quantity of food.
 */
public class FoodGenerationBehavior extends PrimordialEnvironmentBehavior {

  /**
   * {@link ProbabilisticLocalGenerator} that generates a cluster of food.
   */
  public static class FoodLocalGenerator extends ProbabilisticLocalGenerator<EntityType> {

    private FoodLocalGenerator(double probability,
        Table<Integer, Integer, EntityDistribution<EntityType>> entityDistributions) {
      super(probability, entityDistributions);
    }

    public static FoodLocalGenerator newInstance(
        double probability, int radius, RandomGenerator random) {
      return new FoodLocalGenerator(probability, createDistributionMatrix(radius, random));
    }

    private static ImmutableTable<Integer, Integer, EntityDistribution<EntityType>>
        createDistributionMatrix(int radius, RandomGenerator random) {
      ImmutableTable.Builder<Integer, Integer, EntityDistribution<EntityType>> builder =
          ImmutableTable.builder();

      for (int row = 0; row < radius * 2; row++) {
        for (int col = 0; col < radius * 2; col++) {
          double x = col - radius;
          double y = row - radius;
          double p = (radius - Math.sqrt(x * x + y * y)) / radius;
          builder.put(row, col, EntityDistribution.of(EntityType.FOOD, p > 0 ? p : 0, random));
        }
      }
      return builder.build();
    }
  }

  private static Logger logger = LoggerFactory.getLogger(FoodGenerationBehavior.class);

  private int minFoodCount;
  private int foodClusterRadius;
  private int foodInitialEnergy;

  private int foodCount = 0;

  @Override
  public void setState(final PrimordialSimulationModel state,
          final Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment) {
    super.setState(state, environment);
    minFoodCount = state.getFoodMinCount();
    foodClusterRadius = state.getFoodClusterRadius();
    foodInitialEnergy = state.getFoodInitialEnergy();
  }

  @Override
  public void beforeUpdate(PrimordialSimulationModel state,
          Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> env,
          RandomGenerator random) {
    PrimordialEnvironmentModel envModel = env.getModel();
    if (foodCount < minFoodCount) {
      int row = random.nextInt(envModel.getHeight());
      int col = random.nextInt(envModel.getWidth());

      logger.trace("Generating food at ({}, {})", row, col);

      Function<EntityType, EntityModel<EntityType>> entityFactory = entityType -> {
        EntityModel<EntityType> entity = EntityUtil.newEntity(entityType);
        if (entity != null && entityType == EntityType.FOOD) {
          entity.setEnergy(foodInitialEnergy);
          entity.setMaxEnergy(foodInitialEnergy);
        }
        return entity;
      };
      new PrimordialSimulationEnvironmentGenerator(entityFactory, env).apply(
          FoodLocalGenerator.newInstance(1, foodClusterRadius, random), envModel, row, col, random);
    }
  }

  @Override
  public void onAddEntity(EntityModel entity, PrimordialSimulationModel state, PrimordialEnvironmentModel envModel) {
    if (entity instanceof Food) {
      foodCount++;
    }
  }

  @Override
  public void onRemoveEntity(EntityModel entity, PrimordialSimulationModel state, PrimordialEnvironmentModel envModel) {
    if (entity instanceof Food) {
      foodCount--;
    }
  }
}
