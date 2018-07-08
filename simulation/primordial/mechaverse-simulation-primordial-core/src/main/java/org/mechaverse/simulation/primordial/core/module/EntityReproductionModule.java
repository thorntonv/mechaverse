package org.mechaverse.simulation.primordial.core.module;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.SimulationUtil;
import org.mechaverse.simulation.primordial.core.Cell;
import org.mechaverse.simulation.primordial.core.CellEnvironment;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationState;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * An environment simulation module that maintains a target entity population size.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
@SuppressWarnings("unused")
public class EntityReproductionModule implements PrimordialSimulationModule {

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
  public void setState(PrimordialSimulationState state, CellEnvironment env, EntityManager entityManager) {
  }

  @Override
  public void updateState(PrimordialSimulationState state, CellEnvironment env, EntityManager entityManager) {
  }

  @Override
  public void beforeUpdate(PrimordialSimulationState state, CellEnvironment env,
                           EntityManager entityManager, RandomGenerator random) {
    if (entities.size() < entityMaxCount) {
      int row = random.nextInt(env.getRowCount());
      int col = random.nextInt(env.getColumnCount());

      if (env.hasCell(row, col)) {
        Cell cell = env.getCell(row, col);
        if (cell.getEntity(EntityType.ENTITY) == null) {
          PrimordialEntityModel entity = generateRandomEntity(state, random);
          cell.setEntity(entity, EntityType.ENTITY);
          entityManager.addEntity(entity);
        }
      }
    }
  }

  @Override
  public void beforePerformAction(PrimordialSimulationState state, CellEnvironment env,
                                  EntityManager entityManager, RandomGenerator random) {
  }

  @Override
  public void afterUpdate(PrimordialSimulationState state, CellEnvironment env,
                          EntityManager entityManager, RandomGenerator random) {
  }

  private PrimordialEntityModel generateRandomEntity(PrimordialSimulationState state, RandomGenerator random) {
    PrimordialEntityModel entity = new PrimordialEntityModel();
    entity.setId(new UUID(random.nextLong(), random.nextLong()).toString());
    entity.setDirection(SimulationUtil.randomDirection(random));
    entity.setEnergy(entityInitialEnergy);
    entity.setMaxEnergy(entityInitialEnergy);
    return entity;
  }

  @Override
  public void onAddEntity(EntityModel entity, PrimordialSimulationState state) {
    if (entity instanceof PrimordialEntityModel) {
      entities.add((PrimordialEntityModel) entity);
    }
  }

  @Override
  public void onRemoveEntity(EntityModel entity, PrimordialSimulationState state) {
    if (entity instanceof PrimordialEntityModel) {
      entities.remove(entity);
    }
  }

  @VisibleForTesting
  void setEntityMaxCount(int entityMaxCount) {
    this.entityMaxCount = entityMaxCount;
  }
}
