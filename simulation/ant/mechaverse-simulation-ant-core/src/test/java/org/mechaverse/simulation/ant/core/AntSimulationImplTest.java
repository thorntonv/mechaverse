package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.ant.api.util.EntityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * Unit test for {@link AntSimulationImpl}.
 */
public class AntSimulationImplTest {

  private static class RandomActiveEntityProvider extends SimpleActiveEntityProvider {

    @Override
    public Optional<ActiveEntity> getActiveEntity(Entity entity) {
      if (entity instanceof Ant) {
        Ant ant = (Ant) entity;
        return Optional.<ActiveEntity>of(new ActiveAnt(ant, new RandomAntBehavior()));
      }
      return super.getActiveEntity(entity);
    }
  }

  private static class EntityTypeCounter {

    int[] entityTypeCounts = new int[EntityUtil.ENTITY_TYPES.length];

    public void addEntity(Entity entity) {
      entityTypeCounts[EntityUtil.getType(entity).ordinal()]++;
    }

    public void removeEntity(Entity entity) {
      entityTypeCounts[EntityUtil.getType(entity).ordinal()]--;
    }

    public int getEntityCount(EntityType type) {
      return entityTypeCounts[type.ordinal()];
    }
  }

  private static class EntityTypeCountObserver implements EntityManager.Observer {

    private EntityTypeCounter entityTypeCounter = new EntityTypeCounter();

    @Override
    public void onAddEntity(Entity entity) {
      entityTypeCounter.addEntity(entity);
    }

    @Override
    public void onRemoveEntity(Entity entity) {
      entityTypeCounter.removeEntity(entity);
    }

    public int getEntityCount(EntityType type) {
      return entityTypeCounter.getEntityCount(type);
    }
  }

  private static final Logger logger = LoggerFactory.getLogger(AntSimulationImplTest.class);

  private RandomGenerator random;

  @Before
  public void setUp() {
    this.random = new Well19937c();
    random.setSeed(AntSimulationImplTest.class.getName().hashCode());
  }

  @Test
  public void simulate() {
    AntSimulationImpl simulation = new AntSimulationImpl();

    EntityTypeCountObserver entityCountObserver = new EntityTypeCountObserver();
    simulation.addObserver(entityCountObserver);

    verifyEntityTypeCounts(simulation.getState().getModel(), entityCountObserver);

    for (int cnt = 0; cnt < 1*60*60; cnt++) {
      simulation.step();

      verifyEntityTypeCounts(simulation.getState().getModel(), entityCountObserver);
    }

    // TODO(thorntonv): Replace this with the configured min food count.
    assertTrue(entityCountObserver.getEntityCount(EntityType.FOOD) >= 1000);
  }

  @Test
  public void randomGeneratorIsDeterministic() {
    assertEquals(1203944087639818536L, random.nextLong());
  }
  
  @Test
  public void simulate_verifyDeterministic() throws IOException {
    byte[] initialState =
        AntSimulationImpl.randomState(new AntSimulationEnvironmentGenerator(), random).serialize();
    AntSimulationImpl simulation1 = new AntSimulationImpl();
    AntSimulationImpl simulation2 = new AntSimulationImpl();
    simulation1.setState(AntSimulationState.deserialize(initialState));
    simulation2.setState(AntSimulationState.deserialize(initialState));

    for (int cnt = 0; cnt < 100; cnt++) {
      simulation1.step();
      simulation2.step();

      assertArrayEquals(simulation1.getState().serialize(), simulation1.getState().serialize());
    }
  }
  
  @Test
  public void simulate_random() {
    AntSimulationImpl simulation = new AntSimulationImpl(new RandomActiveEntityProvider(), random);
    for (int cnt = 0; cnt < 25000; cnt++) {
      simulation.step();
    }
  }

  private void verifyEntityTypeCounts(SimulationModel model, EntityTypeCountObserver observer) {
    EntityTypeCounter counter = new EntityTypeCounter();

    for (Entity entity : model.getEnvironment().getEntities()) {
      counter.addEntity(entity);
      if (entity instanceof Ant) {
        Entity carriedEntity = ((Ant) entity).getCarriedEntity();
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
