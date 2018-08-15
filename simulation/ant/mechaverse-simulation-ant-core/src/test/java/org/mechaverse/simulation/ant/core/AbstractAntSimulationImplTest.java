package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.core.entity.EntityUtil;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.util.AntSimulationModelUtil;
import org.mechaverse.simulation.common.SimulationObserver;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.mechaverse.simulation.ant.core.AntSimulationTestUtil.assertModelsEqual;

/**
 * Unit test for the ant simulation.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractAntSimulationImplTest {

  private static class EntityTypeCounter {

    int[] entityTypeCounts = new int[EntityUtil.ENTITY_TYPES.length];

    public void addEntity(EntityModel entity) {
      entityTypeCounts[entity.getType().ordinal()]++;
    }

    public void removeEntity(EntityModel entity) {
      entityTypeCounts[entity.getType().ordinal()]--;
    }

    public int getEntityCount(EntityType type) {
      return entityTypeCounts[type.ordinal()];
    }
  }

  private static class EntityTypeCountObserver implements SimulationObserver<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

    private EntityTypeCounter entityTypeCounter = new EntityTypeCounter();

    @Override
    public synchronized void onAddEntity(EntityModel<EntityType> entity, AntSimulationModel state, CellEnvironment envModel) {
      entityTypeCounter.addEntity(entity);
    }

    @Override
    public synchronized void onRemoveEntity(EntityModel<EntityType> entity, AntSimulationModel state, CellEnvironment envModel) {
      entityTypeCounter.removeEntity(entity);
    }

    public synchronized int getEntityCount(EntityType type) {
      return entityTypeCounter.getEntityCount(type);
    }

    @Override
    public void onClose() {}
  }

  private static final Logger logger = LoggerFactory.getLogger(AbstractAntSimulationImplTest.class);

  private RandomGenerator random;

  protected abstract AntSimulationImpl newSimulationImpl();
  protected abstract int testIterationCount();
  protected abstract int smallTestIterationCount();

  @Before
  public void setUp() {
    this.random = RandomUtil.newGenerator(AbstractAntSimulationImplTest.class.getName().hashCode());
  }

  @Test
  public void createSimulation() {
    try (AntSimulationImpl simulation = newSimulationImpl()) {
      assertNotNull(simulation);
    }
  }

  @Test
  public void simulate() {
    try (AntSimulationImpl simulation = newSimulationImpl()) {
      simulation.setState(simulation.generateRandomState());

      EntityTypeCountObserver entityCountObserver = new EntityTypeCountObserver();
      simulation.addObserver(entityCountObserver);

      verifyEntityTypeCounts(simulation.getState(), entityCountObserver);

      for (int cnt = 0; cnt < testIterationCount() / 10; cnt++) {
        simulation.step(10);
        verifyEntityTypeCounts(simulation.getState(), entityCountObserver);
      }
    }
  }

  @Test
  public void randomGeneratorIsDeterministic() {
    assertEquals(244186240197737350L, random.nextLong());
  }

  @Test
  public void simulate_verifyDeterministic() throws Exception {
    byte[] state;
    try (AntSimulationImpl simulation1 = newSimulationImpl();
        AntSimulationImpl simulation2 = newSimulationImpl()) {
      byte[] initialState = AntSimulationModelUtil.serialize(simulation1.generateRandomState());
      assertNotEquals(simulation1, simulation2);
      simulation1.setStateData(initialState);
      simulation2.setStateData(initialState);

      for (int cnt = 0; cnt < smallTestIterationCount() / 10; cnt++) {
        simulation1.step(10);
        simulation2.step(10);
        assertModelsEqual(simulation1.getState(), simulation2.getState());
      }

      state = simulation1.getStateData();
    }

    try (AntSimulationImpl simulation1 = newSimulationImpl();
        AntSimulationImpl simulation2 = newSimulationImpl()) {
      simulation1.setStateData(state);
      simulation2.setStateData(state);

      for (int cnt = 0; cnt < smallTestIterationCount() / 10; cnt++) {
        simulation1.step(10);
        simulation2.step(10);
        assertModelsEqual(simulation1.getState(), simulation2.getState());
      }
    }
  }

  private void verifyEntityTypeCounts(AntSimulationModel model, EntityTypeCountObserver observer) {
    EntityTypeCounter counter = new EntityTypeCounter();

    for(CellEnvironment env : model.getEnvironments()) {
      for (EntityModel<EntityType> entity : env.getEntities()) {
        counter.addEntity(entity);
        if (entity instanceof Ant) {
          EntityModel carriedEntity = ((Ant) entity).getCarriedEntity();
          if (carriedEntity != null) {
            counter.addEntity(carriedEntity);
          }
        }
      }
    }

    for (EntityType entityType : EntityType.values()) {
      int actualCount = counter.getEntityCount(entityType);
      logger.debug("{} count = {}", entityType.name(), actualCount);
      assertEquals(actualCount, observer.getEntityCount(entityType));
    }
  }
}
