package org.mechaverse.simulation.primordial.core.environment;

import com.google.common.annotations.VisibleForTesting;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.SimulationUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialCellModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * An environment simulation module that maintains a target entity population size.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
@SuppressWarnings("unused")
public class EntityReproductionModule extends PrimordialEnvironmentBehavior {

  private static final Logger logger = LoggerFactory.getLogger(EntityReproductionModule.class);

  @Value("#{properties['entityMaxCount']}")
  private int entityMaxCount;
  @Value("#{properties['entityInitialEnergy']}")
  private int entityInitialEnergy;
  @Value("#{properties['entityMinReproductiveAge']}")
  private int entityMinReproductiveAge;

  private final Set<PrimordialEntityModel> entities = new LinkedHashSet<>();

  public EntityReproductionModule() {
  }

  @Override
  public void beforeUpdate(PrimordialSimulationModel state,
      Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> env,
          RandomGenerator random) {
    final PrimordialEnvironmentModel envModel = env.getModel();
    if (entities.size() < entityMaxCount) {
      int row = random.nextInt(envModel.getHeight());
      int col = random.nextInt(envModel.getWidth());

      if (envModel.hasCell(row, col)) {
        PrimordialCellModel cell = envModel.getCell(row, col);
        if (cell.getEntity(EntityType.ENTITY) == null) {
          PrimordialEntityModel entity = generateRandomEntity(state, random);
          cell.setEntity(entity);
          env.addEntity(entity);
        }
      }
    }
  }

  private PrimordialEntityModel generateRandomEntity(SimulationModel state,
      RandomGenerator random) {
    PrimordialEntityModel entity = new PrimordialEntityModel();
    entity.setId(new UUID(random.nextLong(), random.nextLong()).toString());
    entity.setDirection(SimulationUtil.randomDirection(random));
    entity.setEnergy(entityInitialEnergy);
    entity.setMaxEnergy(entityInitialEnergy);
    return entity;
  }

  @Override
  public void onAddEntity(EntityModel<EntityType> entity, PrimordialSimulationModel state,
          PrimordialEnvironmentModel envModel) {
    if (entity instanceof PrimordialEntityModel) {
      entities.add((PrimordialEntityModel) entity);
    }
  }

  @Override
  public void onRemoveEntity(EntityModel<EntityType> entity, PrimordialSimulationModel state,
          PrimordialEnvironmentModel envModel) {
    if (entity instanceof PrimordialEntityModel) {
      entities.remove(entity);
    }
  }

  @VisibleForTesting
  void setEntityMaxCount(int entityMaxCount) {
    this.entityMaxCount = entityMaxCount;
  }
}
