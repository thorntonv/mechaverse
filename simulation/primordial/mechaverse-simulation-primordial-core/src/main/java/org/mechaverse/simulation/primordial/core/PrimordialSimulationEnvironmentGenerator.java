package org.mechaverse.simulation.primordial.core;

import java.util.function.Function;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.cellautomaton.AbstractProbabilisticEnvironmentGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;

import java.util.Collections;
import java.util.UUID;

/**
 * Generator for primordial simulation environments.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
@SuppressWarnings("Unused")
public class PrimordialSimulationEnvironmentGenerator
    extends AbstractProbabilisticEnvironmentGenerator<CellEnvironment, EntityType> {

  private final EntityManager entityManager;
  private final Function<EntityType, EntityModel> entityFactory;

  public PrimordialSimulationEnvironmentGenerator() {
    this(null, RandomUtil.newGenerator());
  }

  public PrimordialSimulationEnvironmentGenerator(EntityManager entityManager, RandomGenerator random) {
    this(EntityUtil::newEntity, entityManager, random);
  }

  public PrimordialSimulationEnvironmentGenerator(Function<EntityType, EntityModel> entityFactory,
                                                  EntityManager entityManager, RandomGenerator random) {
    super(Collections.emptyList());

    this.entityFactory = entityFactory;
    this.entityManager = entityManager;
  }

  @Override
  protected CellEnvironment createEnvironment(int width, int height, RandomGenerator random) {
    EnvironmentModel env = new EnvironmentModel();
    env.setId(UUID.randomUUID().toString());
    env.setWidth(width);
    env.setHeight(height);
    return new CellEnvironment(env);
  }

  @Override
  protected void addEntity(EntityType entityType, int row, int column, CellEnvironment env) {
    if (env.hasCell(row, column)) {
      Cell cell = env.getCell(row, column);

      if (cell.isEmpty()) {
        EntityModel entity = entityFactory.apply(entityType);
        env.addEntity(entity, cell);
        if (entityManager != null) {
          entityManager.addEntity(entity);
        }
      }
    }
  }
}
