package org.mechaverse.simulation.common.cellautomaton;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;

/**
 * A base class for generating environments using entity probability distributions.
 *
 * @param <E> the environment type
 * @param <T> the entity type
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class AbstractProbabilisticEnvironmentGenerator<E, T>
    implements EnvironmentGenerator<E, T> {

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
      pmf.add(new Pair<>(Optional.<T>absent(), absentProbability));
      this.distribution = new EnumeratedDistribution<>(random, pmf);
    }

    public Optional<T> sample() {
      return distribution.sample();
    }

    public static <T> EntityDistribution<T> of(
          T entity, double probability, RandomGenerator random) {
      return new EntityDistribution<T>(ImmutableList.of(new Pair<>(entity, probability)), random);
    }
  }

  /**
   * A {@link LocalGenerator} implementation that generates entities based on probability
   * distributions.
   *
   * @param <T> the entity type
   */
  public static class ProbabilisticLocalGenerator<T> implements LocalGenerator<T> {

    private double probability;
    private Table<Integer, Integer, EntityDistribution<T>> entityDistributions;

    public ProbabilisticLocalGenerator(double probability,
        Table<Integer, Integer, EntityDistribution<T>>  entityDistributions) {
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
    public Optional<T> generateEntity(int row, int column) {
      EntityDistribution<T> entityDistribution = entityDistributions.get(row, column);
      return entityDistribution != null ? entityDistribution.sample() : Optional.<T>absent();
    }
  }

  private List<ProbabilisticLocalGenerator<T>> localGenerators = new ArrayList<>();

  /**
   * Creates a new environment with the given dimensions.
   */
  protected abstract E createEnvironment(int width, int height, RandomGenerator random);

  /**
   * Adds an entity of the given type at the specified position in the environment.
   */
  protected abstract void addEntity(T entity, int row, int column, E env);

  public AbstractProbabilisticEnvironmentGenerator(
      List<ProbabilisticLocalGenerator<T>> localGenerators) {
    this.localGenerators = localGenerators;
  }

  @Override
  public final E generate(int width, int height, RandomGenerator random) {
    E env = createEnvironment(width, height, random);

    for (ProbabilisticLocalGenerator<T> localGenerator : localGenerators) {
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
  public List<T> apply(LocalGenerator<T> localGenerator,
      E env, int originRow, int originCol, RandomGenerator random) {
    List<T> generatedEntities = new ArrayList<T>();
    int rowOffset = originRow - localGenerator.getHeight() / 2;
    int colOffset = originCol - localGenerator.getWidth() / 2;

    for (int row = 0; row < localGenerator.getHeight(); row++) {
      for (int col = 0; col < localGenerator.getWidth(); col++) {
        Optional<T> entity = localGenerator.generateEntity(row, col);

        if (entity.isPresent()) {
          addEntity(entity.get(), rowOffset + row, colOffset + col, env);
          generatedEntities.add(entity.get());
        }
      }
    }
    return generatedEntities;
  }
}
