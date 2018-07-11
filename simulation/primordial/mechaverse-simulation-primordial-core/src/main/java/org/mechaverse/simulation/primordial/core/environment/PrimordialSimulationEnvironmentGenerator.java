package org.mechaverse.simulation.primordial.core.environment;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.AbstractProbabilisticEnvironmentModelGenerator;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialCellEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialCellModel;

/**
 * Generator for primordial simulation environments.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
@SuppressWarnings("Unused")
public class PrimordialSimulationEnvironmentGenerator
    extends AbstractProbabilisticEnvironmentModelGenerator<PrimordialCellEnvironmentModel, EntityModel, EntityType> {

  private final EntityManager<SimulationModel, EntityModel> entityManager;
  private final Function<EntityType, EntityModel> entityFactory;

  public PrimordialSimulationEnvironmentGenerator() {
    this(null);
  }

  public PrimordialSimulationEnvironmentGenerator(
      EntityManager<SimulationModel, EntityModel> entityManager) {
    this(EntityUtil::newEntity, entityManager);
  }

  public PrimordialSimulationEnvironmentGenerator(Function<EntityType, EntityModel> entityFactory,
      EntityManager<SimulationModel, EntityModel> entityManager) {
    super(Collections.emptyList());

    this.entityFactory = entityFactory;
    this.entityManager = entityManager;
  }

  public PrimordialCellEnvironmentModel generate(RandomGenerator randomGenerator) {
    return generate(200, 200, randomGenerator);
  }

  @Override
  protected PrimordialCellEnvironmentModel createEnvironment(int width, int height,
      RandomGenerator random) {
    PrimordialCellEnvironmentModel env = new PrimordialCellEnvironmentModel();
    env.setId(UUID.randomUUID().toString());
    env.setWidth(width);
    env.setHeight(height);
    return env;
  }

  @Override
  protected EntityModel addEntity(EntityType entityType, int row, int column,
      PrimordialCellEnvironmentModel env) {
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
