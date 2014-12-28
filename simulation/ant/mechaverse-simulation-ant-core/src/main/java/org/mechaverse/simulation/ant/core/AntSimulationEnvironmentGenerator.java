package org.mechaverse.simulation.ant.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.Nest;
import org.mechaverse.simulation.ant.api.util.EntityUtil;
import org.mechaverse.simulation.common.cellautomaton.AbstractProbabilisticEnvironmentGenerator;
import org.mechaverse.simulation.common.util.RandomUtil;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;

/**
 * Generator for ant simulation environments.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class AntSimulationEnvironmentGenerator
    extends AbstractProbabilisticEnvironmentGenerator<CellEnvironment, EntityType> {

  private final EntityManager entityManager;
  private final Function<EntityType, Entity> entityFactory;

  public static ProbabilisticLocalGenerator<EntityType> newRockGenerator(RandomGenerator random) {
    return new ProbabilisticLocalGenerator<EntityType>(.01,
        ImmutableTable.<Integer, Integer, EntityDistribution<EntityType>>builder()
            .put(0, 0, EntityDistribution.of(EntityType.ROCK, .50, random))
            .put(0, 1, EntityDistribution.of(EntityType.ROCK, .75, random))
            .put(0, 2, EntityDistribution.of(EntityType.ROCK, .50, random))
            .put(1, 0, EntityDistribution.of(EntityType.ROCK, .75, random))
            .put(1, 1, EntityDistribution.of(EntityType.ROCK, .90, random))
            .put(1, 2, EntityDistribution.of(EntityType.ROCK, .75, random))
            .put(2, 0, EntityDistribution.of(EntityType.ROCK, .50, random))
            .put(2, 1, EntityDistribution.of(EntityType.ROCK, .75, random))
            .put(2, 2, EntityDistribution.of(EntityType.ROCK, .50, random))
            .build());
  }

  public AntSimulationEnvironmentGenerator() {
    this(null, RandomUtil.newGenerator());
  }

  public AntSimulationEnvironmentGenerator(EntityManager entityManager, RandomGenerator random) {
    this(new Function<EntityType, Entity>() {

      @Override
      public Entity apply(EntityType entityType) {
        return EntityUtil.newEntity(entityType);
      }
    }, entityManager, random);
  }

  public AntSimulationEnvironmentGenerator(Function<EntityType, Entity> entityFactory,
      EntityManager entityManager, RandomGenerator random) {
    super(ImmutableList.of(newRockGenerator(random)));

    this.entityFactory = entityFactory;
    this.entityManager = entityManager;
  }

  @Override
  protected CellEnvironment createEnvironment(int width, int height, RandomGenerator random) {
    Environment env = new Environment();
    env.setId(UUID.randomUUID().toString());
    env.setWidth(width);
    env.setHeight(height);

    CellEnvironment cells = new CellEnvironment(env);
    Cell cell = getRandomEmptyCell(cells, random);
    if(cell != null) {
      Nest nest = new Nest();
      nest.setId(UUID.randomUUID().toString());
      cells.addEntity(nest, cell);
    }

    return cells;
  }

  @Override
  protected void addEntity(EntityType entityType, int row, int column, CellEnvironment env) {
    if (env.hasCell(row, column)) {
      Cell cell = env.getCell(row, column);

      if (cell.isEmpty()) {
        Entity entity = entityFactory.apply(entityType);
        env.addEntity(entity, cell);
        if (entityManager != null) {
          entityManager.addEntity(entity);
        }
      }
    }
  }

  private Cell getRandomEmptyCell(CellEnvironment env, RandomGenerator random) {
    List<Cell> emptyCells = new ArrayList<>();
    for (int row = 0; row < env.getRowCount(); row++) {
      for (int col = 0; col < env.getColumnCount(); col++) {
        Cell cell = env.getCell(row, col);
        if (cell.isEmpty()) {
          emptyCells.add(cell);
        }
      }
    }

    return emptyCells.size() > 0 ? emptyCells.get(random.nextInt(emptyCells.size())) : null;
  }
}
