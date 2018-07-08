package org.mechaverse.simulation.primordial.core;

import com.google.common.base.Function;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.cellautomaton.AbstractProbabilisticEnvironmentGenerator;
import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.common.model.Environment;
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
public class PrimordialSimulationEnvironmentGenerator
    extends AbstractProbabilisticEnvironmentGenerator<CellEnvironment, EntityType> {

  private final EntityManager entityManager;
  private final Function<EntityType, Entity> entityFactory;

  public PrimordialSimulationEnvironmentGenerator() {
    this(null, RandomUtil.newGenerator());
  }

  public PrimordialSimulationEnvironmentGenerator(EntityManager entityManager, RandomGenerator random) {
    this(new Function<EntityType, Entity>() {

      @Override
      public Entity apply(EntityType entityType) {
        return EntityUtil.newEntity(entityType);
      }
    }, entityManager, random);
  }

  public PrimordialSimulationEnvironmentGenerator(Function<EntityType, Entity> entityFactory,
                                                  EntityManager entityManager, RandomGenerator random) {
    super(Collections.<ProbabilisticLocalGenerator<EntityType>>emptyList());

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
}
