package org.mechaverse.simulation.ant.core.environment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.entity.EntityUtil;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.Cell;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.model.Nest;
import org.mechaverse.simulation.common.AbstractProbabilisticEnvironmentModelGenerator;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.RandomUtil;

/**
 * Generator for ant simulation environments.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class AntSimulationEnvironmentGenerator
    extends AbstractProbabilisticEnvironmentModelGenerator<CellEnvironment, EntityModel<EntityType>, EntityType> {

  private final EntityManager<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> entityManager;
  private final Function<EntityType, EntityModel<EntityType>> entityFactory;

  public static ProbabilisticLocalGenerator<EntityType> newRockGenerator(RandomGenerator random) {
    return new ProbabilisticLocalGenerator<>(.01, ImmutableTable.<Integer, Integer, EntityDistribution<EntityType>>builder().put(0, 0, EntityDistribution.of(EntityType.ROCK, .50, random)).put(0, 1, EntityDistribution.of(EntityType.ROCK, .75, random)).put(0, 2, EntityDistribution.of(EntityType.ROCK, .50, random)).put(1, 0, EntityDistribution.of(EntityType.ROCK, .75, random)).put(1, 1, EntityDistribution.of(EntityType.ROCK, .90, random)).put(1, 2, EntityDistribution.of(EntityType.ROCK, .75, random)).put(2, 0, EntityDistribution.of(EntityType.ROCK, .50, random)).put(2, 1, EntityDistribution.of(EntityType.ROCK, .75, random)).put(2, 2, EntityDistribution.of(EntityType.ROCK, .50, random)).build());
  }

  public AntSimulationEnvironmentGenerator() {
    this(null, RandomUtil.newGenerator());
  }

  public AntSimulationEnvironmentGenerator(
      EntityManager<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> entityManager, RandomGenerator random) {
    this(EntityUtil::newEntity, entityManager, random);
  }

  public AntSimulationEnvironmentGenerator(Function<EntityType, EntityModel<EntityType>> entityFactory,
          EntityManager<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> entityManager, RandomGenerator random) {
    super(ImmutableList.of(newRockGenerator(random)));

    this.entityFactory = entityFactory;
    this.entityManager = entityManager;
  }

  @Override
  public CellEnvironment generate(RandomGenerator random) {
    return generate(200, 200, random);
  }

  @Override
  protected CellEnvironment createEnvironment(int width, int height, RandomGenerator random) {
    CellEnvironment env = new CellEnvironment();
    env.setId(UUID.randomUUID().toString());
    env.setWidth(width);
    env.setHeight(height);

    Cell cell = getRandomEmptyCell(env, random);
    if(cell != null) {
      Nest nest = new Nest();
      nest.setId(UUID.randomUUID().toString());
      env.addEntity(nest, cell);
    }

    return env;
  }

  @Override
  protected EntityModel<EntityType> addEntity(EntityType entityType, int row, int column, CellEnvironment env) {
    if (env.hasCell(row, column)) {
      Cell cell = env.getCell(row, column);

      if (cell.isEmpty()) {
        EntityModel<EntityType> entity = entityFactory.apply(entityType);
        env.addEntity(entity, cell);
        if (entityManager != null) {
          entityManager.addEntity(entity);
        }
        return entity;
      }
    }
    return null;
  }

  private Cell getRandomEmptyCell(CellEnvironment env, RandomGenerator random) {
    List<Cell> emptyCells = new ArrayList<>();
    for (int row = 0; row < env.getHeight(); row++) {
      for (int col = 0; col < env.getWidth(); col++) {
        Cell cell = env.getCell(row, col);
        if (cell.isEmpty()) {
          emptyCells.add(cell);
        }
      }
    }

    return emptyCells.size() > 0 ? emptyCells.get(random.nextInt(emptyCells.size())) : null;
  }
}
