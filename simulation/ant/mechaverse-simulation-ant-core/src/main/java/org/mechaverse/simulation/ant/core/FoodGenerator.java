package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Food;
import org.mechaverse.simulation.ant.api.util.EntityUtil;
import org.mechaverse.simulation.common.cellautomata.AbstractProbabilisticEnvironmentGenerator.EntityDistribution;
import org.mechaverse.simulation.common.cellautomata.AbstractProbabilisticEnvironmentGenerator.ProbabilisticLocalGenerator;
import org.mechaverse.simulation.common.cellautomata.EnvironmentGenerator;

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

  // TODO(thorntonv): Make these values configurable.
  private int minFoodCount = 1000;
  private int foodClusterRadius = 15;
  private int foodEnergy = 300;

  private int foodCount = 0;

  private final EnvironmentGenerator<CellEnvironment, EntityType> environmentGenerator =
      new AntSimulationEnvironmentGenerator(new Function<EntityType, Entity>() {
        @Override
        public Entity apply(EntityType entityType) {
          Entity entity = EntityUtil.newEntity(entityType);
          onAddEntity(entity);
          return entity;
        }
      });

  private final FoodLocalGenerator foodLocalGenerator;


  public FoodGenerator() {
    this.foodLocalGenerator = FoodLocalGenerator.newInstance(1, foodClusterRadius);
  }

  @Override
  public void update(CellEnvironment env, EntityManager entityManager, RandomGenerator random) {
    if (foodCount < minFoodCount) {
      int row = random.nextInt(env.getRowCount());
      int col = random.nextInt(env.getColumnCount());

      environmentGenerator.apply(foodLocalGenerator, env, row, col, random);
    }
  }

  @Override
  public void onAddEntity(Entity entity) {
    if (entity instanceof Food) {
      entity.setEnergy(foodEnergy);
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
