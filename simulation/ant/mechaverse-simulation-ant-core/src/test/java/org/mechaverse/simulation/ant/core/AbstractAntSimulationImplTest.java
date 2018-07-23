package org.mechaverse.simulation.ant.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.core.entity.EntityUtil;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.util.AntSimulationModelUtil;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.Assert.*;
import static org.mechaverse.simulation.ant.core.AntSimulationTestUtil.assertModelsEqual;
import static org.mechaverse.simulation.common.util.ArrayUtil.toByteArray;

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

  private static class EntityTypeCountObserver implements EntityManager.Observer<AntSimulationModel, EntityModel<EntityType>> {

    private EntityTypeCounter entityTypeCounter = new EntityTypeCounter();

    @Override
    public void onAddEntity(EntityModel entity, AntSimulationModel state) {
      entityTypeCounter.addEntity(entity);
    }

    @Override
    public void onRemoveEntity(EntityModel entity, AntSimulationModel state) {
      entityTypeCounter.removeEntity(entity);
    }

    public int getEntityCount(EntityType type) {
      return entityTypeCounter.getEntityCount(type);
    }
  }

  private static final Logger logger = LoggerFactory.getLogger(AbstractAntSimulationImplTest.class);

  @Value("#{properties['antMaxCount']}") private int antMaxCount;
  @Value("#{properties['foodMinCount']}") private int minFoodCount;

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
    assertNotNull(newSimulationImpl());
  }

  @Test
  public void simulate() throws IOException {
    AntSimulationImpl simulation = newSimulationImpl();
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
    assertEquals(244186240197737350L, random.nextLong());
  }

  @Test
  public void simulate_verifyDeterministic() throws IOException {
    AntSimulationImpl simulation1 = newSimulationImpl();
    AntSimulationImpl simulation2 = newSimulationImpl();

    byte[] initialState = AntSimulationModelUtil.serialize(simulation1.generateRandomState());
    assertNotEquals(simulation1, simulation2);
    simulation1.setState(AntSimulationModelUtil.deserialize(new GZIPInputStream(new ByteArrayInputStream(initialState))));
    simulation2.setState(AntSimulationModelUtil.deserialize(new GZIPInputStream(new ByteArrayInputStream(initialState))));

    for (int cnt = 0; cnt < smallTestIterationCount(); cnt++) {
      simulation1.step();
      simulation2.step();

      assertModelsEqual(simulation1.getState(), simulation2.getState());
    }

    byte[] state = AntSimulationModelUtil.serialize(simulation1.getState());
    simulation1 = newSimulationImpl();
    simulation1.setState(AntSimulationModelUtil.deserialize(new GZIPInputStream(new ByteArrayInputStream(state))));
    simulation2 = newSimulationImpl();
    simulation2.setState(AntSimulationModelUtil.deserialize(new GZIPInputStream(new ByteArrayInputStream(state))));

    for (int cnt = 0; cnt < smallTestIterationCount(); cnt++) {
      simulation1.step();
      simulation2.step();

      assertModelsEqual(simulation1.getState(), simulation2.getState());
    }
  }

  private void verifyEntityTypeCounts(AntSimulationModel model, EntityTypeCountObserver observer) {
    EntityTypeCounter counter = new EntityTypeCounter();

    for (EntityModel<EntityType> entity : model.getEnvironment().getEntities()) {
      counter.addEntity(entity);
      if (entity instanceof Ant) {
        EntityModel carriedEntity = ((Ant) entity).getCarriedEntity();
        if (carriedEntity != null) {
          counter.addEntity(carriedEntity);
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
