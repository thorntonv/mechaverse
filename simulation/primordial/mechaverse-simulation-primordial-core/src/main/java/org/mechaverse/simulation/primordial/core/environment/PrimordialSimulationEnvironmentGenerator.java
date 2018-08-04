package org.mechaverse.simulation.primordial.core.environment;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.AbstractProbabilisticEnvironmentModelGenerator;
import org.mechaverse.simulation.common.Environment;
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

  private final Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment;
  private final Function<EntityType, EntityModel<EntityType>> entityFactory;

  public PrimordialSimulationEnvironmentGenerator() {
    this(null);
  }

  public PrimordialSimulationEnvironmentGenerator(
          Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment) {
    this(EntityUtil::newEntity, environment);
  }

  public PrimordialSimulationEnvironmentGenerator(Function<EntityType, EntityModel<EntityType>> entityFactory,
          Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment) {
    super(Collections.emptyList());

    this.entityFactory = entityFactory;
    this.environment = environment;
  }

  public PrimordialEnvironmentModel generate(RandomGenerator randomGenerator) {
    return generate(125, 125, randomGenerator);
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
  protected EntityModel<EntityType> addEntity(EntityType entityType, int row, int column,
      PrimordialEnvironmentModel env) {
    if (env.hasCell(row, column)) {
      PrimordialCellModel cell = env.getCell(row, column);

      if (cell.isEmpty()) {
        EntityModel<EntityType> entity = entityFactory.apply(entityType);
        env.addEntity(entity, cell);
        if (environment != null) {
          environment.addEntity(entity);
        }
        return entity;
      }
    }
    return null;
  }
}
