package org.mechaverse.simulation.common;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;

/**
 * A base class for generating random environments using entity probability distributions.
 *
 * @param <ENV_MODEL> the environment model type
 * @param <ENT_MODEL> the entity model type
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractProbabilisticEnvironmentModelGenerator<
    ENV_MODEL extends EnvironmentModel,
    ENT_MODEL extends EntityModel,
    ENT_TYPE extends Enum<ENT_TYPE>> implements EnvironmentModelGenerator<ENV_MODEL, ENT_MODEL, ENT_TYPE> {

  /**
   * An entity probability distribution. Any unused part of the pmf is assigned to the absent entity
   * option.
   *
   * @param <T> the entity type
   */
  public static class EntityDistribution<T> {

    private EnumeratedDistribution<Optional<T>> distribution;

    public EntityDistribution(List<Pair<T, Double>> entityProbabilities, RandomGenerator random) {
      List<Pair<Optional<T>, Double>> pmf = new ArrayList<>();
      double absentProbability = 1.0;
      for (Pair<T, Double> entityProbability : entityProbabilities) {
        absentProbability -= entityProbability.getSecond();
        pmf.add(new Pair<>(Optional.of(entityProbability.getKey()), entityProbability.getValue()));
      }
      pmf.add(new Pair<>(Optional.empty(), absentProbability));
      this.distribution = new EnumeratedDistribution<>(random, pmf);
    }

    public Optional<T> sample() {
      return distribution.sample();
    }

    public static <T> EntityDistribution<T> of(
          T entity, double probability, RandomGenerator random) {
      return new EntityDistribution<>(ImmutableList.of(new Pair<>(entity, probability)), random);
    }
  }

  /**
   * A {@link LocalGenerator} implementation that generates entities based on probability
   * distributions.
   *
   * @param <ENT_TYPE> the entity model type
   */
  public static class ProbabilisticLocalGenerator<ENT_TYPE extends Enum<ENT_TYPE>>
      implements LocalGenerator<ENT_TYPE> {

    private double probability;
    private Table<Integer, Integer, EntityDistribution<ENT_TYPE>> entityDistributions;

    public ProbabilisticLocalGenerator(double probability,
        Table<Integer, Integer, EntityDistribution<ENT_TYPE>>  entityDistributions) {
      super();
      this.probability = probability;
      this.entityDistributions = entityDistributions;
    }

    @Override
    public int getWidth() {
      return entityDistributions.columnKeySet().size();
    }

    @Override
    public int getHeight() {
      return entityDistributions.rowKeySet().size();
    }

    public double getProbability() {
      return probability;
    }

    @Override
    public Optional<ENT_TYPE> generateEntity(int row, int column) {
      EntityDistribution<ENT_TYPE> entityDistribution = entityDistributions.get(row, column);
      return entityDistribution != null ? entityDistribution.sample() : Optional.empty();
    }
  }

  private List<ProbabilisticLocalGenerator<ENT_TYPE>> localGenerators;

  /**
   * Creates a new environment with the given dimensions.
   */
  protected abstract ENV_MODEL createEnvironment(int width, int height, RandomGenerator random);

  /**
   * Adds an entity of the given type at the specified position in the environment.
   */
  protected abstract ENT_MODEL addEntity(ENT_TYPE entity, int row, int column, ENV_MODEL env);

  public AbstractProbabilisticEnvironmentModelGenerator(
      List<ProbabilisticLocalGenerator<ENT_TYPE>> localGenerators) {
    this.localGenerators = localGenerators;
  }

  @Override
  public final ENV_MODEL generate(int width, int height, RandomGenerator random) {
    ENV_MODEL env = createEnvironment(width, height, random);

    for (ProbabilisticLocalGenerator<ENT_TYPE> localGenerator : localGenerators) {
      BinomialDistribution distribution =
          new BinomialDistribution(random, width * height, localGenerator.getProbability());
      int count = distribution.sample();
      for (int cnt = 1; cnt <= count; cnt++) {
        int originRow = random.nextInt(height);
        int originCol = random.nextInt(width);
        apply(localGenerator, env, originRow, originCol, random);
      }
    }

    return env;
  }

  @Override
  public List<ENT_MODEL> apply(LocalGenerator<ENT_TYPE> localGenerator,
      ENV_MODEL env, int originRow, int originCol, RandomGenerator random) {
    List<ENT_MODEL> generatedEntities = new ArrayList<>();
    int rowOffset = originRow - localGenerator.getHeight() / 2;
    int colOffset = originCol - localGenerator.getWidth() / 2;

    for (int row = 0; row < localGenerator.getHeight(); row++) {
      for (int col = 0; col < localGenerator.getWidth(); col++) {
        Optional<ENT_TYPE> entityType = localGenerator.generateEntity(row, col);

        if (entityType.isPresent()) {
          ENT_MODEL entityModel = addEntity(entityType.get(), rowOffset + row, colOffset + col, env);
          if(entityModel != null) {
            generatedEntities.add(entityModel);
          }
        }
      }
    }
    return generatedEntities;
  }
}
