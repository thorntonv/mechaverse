package org.mechaverse.simulation.primordial.core.environment;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.AbstractProbabilisticEnvironmentModelGenerator;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialCellModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

/**
 * Generator for primordial simulation environments.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
@SuppressWarnings("Unused")
public class PrimordialSimulationEnvironmentGenerator
    extends AbstractProbabilisticEnvironmentModelGenerator<PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

  private final EntityManager<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> entityManager;
  private final Function<EntityType, EntityModel> entityFactory;

  public PrimordialSimulationEnvironmentGenerator() {
    this(null);
  }

  public PrimordialSimulationEnvironmentGenerator(
          EntityManager<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> entityManager) {
    this(EntityUtil::newEntity, entityManager);
  }

  public PrimordialSimulationEnvironmentGenerator(Function<EntityType, EntityModel> entityFactory,
          EntityManager<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> entityManager) {
    super(Collections.emptyList());

    this.entityFactory = entityFactory;
    this.entityManager = entityManager;
  }

  public PrimordialEnvironmentModel generate(RandomGenerator randomGenerator) {
    return generate(200, 200, randomGenerator);
  }

  @Override
  protected PrimordialEnvironmentModel createEnvironment(int width, int height,
      RandomGenerator random) {
    PrimordialEnvironmentModel env = new PrimordialEnvironmentModel();
    env.setId(UUID.randomUUID().toString());
    env.setWidth(width);
    env.setHeight(height);
    return env;
  }

  @Override
  protected EntityModel addEntity(EntityType entityType, int row, int column,
      PrimordialEnvironmentModel env) {
    if (env.hasCell(row, column)) {
      PrimordialCellModel cell = env.getCell(row, column);

      if (cell.isEmpty()) {
        EntityModel entity = entityFactory.apply(entityType);
        env.addEntity(entity, cell);
        if (entityManager != null) {
          entityManager.addEntity(entity);
        }
        return entity;
      }
    }
    return null;
  }
}
