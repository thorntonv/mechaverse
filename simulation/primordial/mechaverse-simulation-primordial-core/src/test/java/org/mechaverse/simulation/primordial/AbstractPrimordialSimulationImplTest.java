package org.mechaverse.simulation.primordial;

import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.common.SimulationObserver;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationImpl;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.mechaverse.simulation.primordial.core.util.PrimordialSimulationModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Unit test for the ant simulation.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractPrimordialSimulationImplTest {

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

  private static class EntityTypeCountObserver implements SimulationObserver<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

    private EntityTypeCounter entityTypeCounter = new EntityTypeCounter();

    @Override
    public void onAddEntity(EntityModel<EntityType> entity, PrimordialSimulationModel state, PrimordialEnvironmentModel envModel) {
      entityTypeCounter.addEntity(entity);
    }

    @Override
    public void onRemoveEntity(EntityModel<EntityType> entity, PrimordialSimulationModel state, PrimordialEnvironmentModel envModel) {
      entityTypeCounter.removeEntity(entity);
    }

    public int getEntityCount(EntityType type) {
      return entityTypeCounter.getEntityCount(type);
    }

  }

  private static final Logger logger = LoggerFactory.getLogger(AbstractPrimordialSimulationImplTest.class);

  private RandomGenerator random;

  protected abstract PrimordialSimulationImpl newSimulationImpl();
  protected abstract int testIterationCount();
  protected abstract int smallTestIterationCount();

  @Before
  public void setUp() {
    this.random = RandomUtil.newGenerator(AbstractPrimordialSimulationImplTest.class.getName().hashCode());
  }

  @Test
  public void createSimulation() {
    assertNotNull(newSimulationImpl());
  }

  @Test
  public void simulate() {
    PrimordialSimulationImpl simulation = newSimulationImpl();
    simulation.setState(simulation.generateRandomState());

    EntityTypeCountObserver entityCountObserver = new EntityTypeCountObserver();
    simulation.addObserver(entityCountObserver);

    verifyEntityTypeCounts(simulation.getState(), entityCountObserver);

    for (int cnt = 0; cnt < testIterationCount(); cnt++) {
      simulation.step();

      verifyEntityTypeCounts(simulation.getState(), entityCountObserver);
    }
  }

  @Test
  public void randomGeneratorIsDeterministic() {
    assertEquals(2639974915846087211L, random.nextLong());
  }

  @Test
  public void simulate_verifyDeterministic() throws Exception {
    PrimordialSimulationImpl simulation1 = newSimulationImpl();
    PrimordialSimulationImpl simulation2 = newSimulationImpl();

    byte[] initialState = PrimordialSimulationModelUtil.serialize(simulation1.generateRandomState());
    assertNotEquals(simulation1, simulation2);
    simulation1.setStateData(initialState);
    simulation2.setStateData(initialState);

    for (int cnt = 0; cnt < smallTestIterationCount(); cnt++) {
      simulation1.step();
      simulation2.step();

      assertModelsEqual(simulation1.getState(), simulation2.getState());
    }

    byte[] state = simulation1.getStateData();
    simulation1 = newSimulationImpl();
    simulation1.setStateData(state);
    simulation2 = newSimulationImpl();
    simulation2.setStateData(state);

    for (int cnt = 0; cnt < smallTestIterationCount(); cnt++) {
      simulation1.step();
      simulation2.step();

      assertModelsEqual(simulation1.getState(), simulation2.getState());
    }
  }

  private void verifyEntityTypeCounts(PrimordialSimulationModel model, EntityTypeCountObserver observer) {
    EntityTypeCounter counter = new EntityTypeCounter();

    for (EntityModel<EntityType> entity : model.getEnvironment().getEntities()) {
      counter.addEntity(entity);
    }

    for (EntityType entityType : EntityType.values()) {
      int actualCount = counter.getEntityCount(entityType);
      logger.debug("{} count = {}", entityType.name(), actualCount);
      assertEquals(actualCount, observer.getEntityCount(entityType));
    }
  }

  public static void assertModelsEqual(PrimordialSimulationModel expected, PrimordialSimulationModel actual)
          throws IOException {
    assertEquals(expected.getEnvironment().toString(), actual.getEnvironment().toString());

    // Sort the entities so that order will not cause the comparison to fail.
    for (PrimordialEnvironmentModel env : expected.getEnvironments()) {
      env.getEntities().sort(EntityUtil.ENTITY_ORDERING);
    }
    for (PrimordialEnvironmentModel env : actual.getEnvironments()) {
      env.getEntities().sort(EntityUtil.ENTITY_ORDERING);
    }

    assertArrayEquals(PrimordialSimulationModelUtil.serialize(expected), PrimordialSimulationModelUtil.serialize(actual));
  }
}