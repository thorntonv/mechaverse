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
import org.mechaverse.simulation.common.cellautomata.AbstractProbabilisticEnvironmentGenerator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;

/**
 * Generator for ant simulation environments.
 *
 * @author thorntonv@mechaverse.org
 */
public class AntSimulationEnvironmentGenerator
    extends AbstractProbabilisticEnvironmentGenerator<CellEnvironment, EntityType> {

  private static final ProbabilisticLocalGenerator<EntityType> ROCK_GENERATOR =
      new ProbabilisticLocalGenerator<EntityType>(.01,
          ImmutableTable.<Integer, Integer, EntityDistribution<EntityType>>builder()
              .put(0, 0, EntityDistribution.of(EntityType.ROCK, .50))
              .put(0, 1, EntityDistribution.of(EntityType.ROCK, .75))
              .put(0, 2, EntityDistribution.of(EntityType.ROCK, .50))
              .put(1, 0, EntityDistribution.of(EntityType.ROCK, .75))
              .put(1, 1, EntityDistribution.of(EntityType.ROCK, .90))
              .put(1, 2, EntityDistribution.of(EntityType.ROCK, .75))
              .put(2, 0, EntityDistribution.of(EntityType.ROCK, .50))
              .put(2, 1, EntityDistribution.of(EntityType.ROCK, .75))
              .put(2, 2, EntityDistribution.of(EntityType.ROCK, .50))
              .build());

  private final EntityManager entityManager;

  public AntSimulationEnvironmentGenerator() {
    this(null);
  }

  public AntSimulationEnvironmentGenerator(EntityManager entityManager) {
    super(ImmutableList.of(ROCK_GENERATOR));

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
        Entity entity = EntityUtil.newEntity(entityType);
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
