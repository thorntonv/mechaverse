package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mechaverse.simulation.ant.core.AntSimulationTestUtil.assertModelsEqual;
import static org.mechaverse.simulation.ant.core.AntSimulationTestUtil.assertStatesEqual;
import static org.mechaverse.simulation.common.datastore.SimulationDataStoreOutputStream.toByteArray;

import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.core.environment.AntSimulationEnvironmentGenerator;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.ant.core.entity.EntityUtil;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Unit test for {@link AntSimulationImpl}.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractAntSimulationImplTest {

  private static class EntityTypeCounter {

    int[] entityTypeCounts = new int[EntityUtil.ENTITY_TYPES.length];

    public void addEntity(EntityModel entity) {
      entityTypeCounts[EntityUtil.getType(entity).ordinal()]++;
    }

    public void removeEntity(EntityModel entity) {
      entityTypeCounts[EntityUtil.getType(entity).ordinal()]--;
    }

    public int getEntityCount(EntityType type) {
      return entityTypeCounts[type.ordinal()];
    }
  }

  private static class EntityTypeCountObserver implements EntityManager.Observer<SimulationModel, AntSimulationState> {

    private EntityTypeCounter entityTypeCounter = new EntityTypeCounter();

    @Override
    public void onAddEntity(EntityModel entity, AntSimulationState state) {
      entityTypeCounter.addEntity(entity);
    }

    @Override
    public void onRemoveEntity(EntityModel entity, AntSimulationState state) {
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

    verifyEntityTypeCounts(simulation.getState().getModel(), entityCountObserver);

    for (int cnt = 0; cnt < testIterationCount(); cnt++) {
      simulation.step();

      verifyEntityTypeCounts(simulation.getState().getModel(), entityCountObserver);
    }
  }

  @Test
  public void randomGeneratorIsDeterministic() {
    assertEquals(244186240197737350L, random.nextLong());
  }

  @Test
  public void simulate_verifyDeterministic() throws IOException {
    byte[] initialState = toByteArray(AntSimulationImpl.randomState(
        new AntSimulationEnvironmentGenerator(), random));
    AntSimulationImpl simulation1 = newSimulationImpl();
    AntSimulationImpl simulation2 = newSimulationImpl();
    assertNotEquals(simulation1, simulation2);
    simulation1.setState(MemorySimulationDataStore.fromByteArray(initialState));
    simulation2.setState(MemorySimulationDataStore.fromByteArray(initialState));

    for (int cnt = 0; cnt < smallTestIterationCount(); cnt++) {
      simulation1.step();
      simulation2.step();

      assertStatesEqual(simulation1.getState(), simulation2.getState());
    }

    byte[] state = toByteArray(simulation1.getState());
    simulation1 = newSimulationImpl();
    simulation1.setState(MemorySimulationDataStore.fromByteArray(state));
    simulation2 = newSimulationImpl();
    simulation2.setState(MemorySimulationDataStore.fromByteArray(state));

    for (int cnt = 0; cnt < smallTestIterationCount(); cnt++) {
      simulation1.step();
      simulation2.step();

      assertStatesEqual(simulation1.getState(), simulation2.getState());
    }
  }

  @Test
  public void simulation_verifyReplay() throws IOException {
    // Generate the initial state.
    SimulationDataStore initialState =
        AntSimulationImpl.randomState(new AntSimulationEnvironmentGenerator(null, random), random);
    AntSimulationImpl simulation = newSimulationImpl();
    simulation.setState(new MemorySimulationDataStore(initialState));
    for (int cnt = 0; cnt < testIterationCount(); cnt++) {
      simulation.step();
    }
    initialState = simulation.getState();

    simulation = newSimulationImpl();
    simulation.setState(new MemorySimulationDataStore(initialState));
    for (int cnt = 0; cnt < smallTestIterationCount(); cnt++) {
      simulation.step();
    }

    try(ClassPathXmlApplicationContext replayCtx =
        new ClassPathXmlApplicationContext("test-simulation-context-replay.xml")) {
      AntSimulationImpl replaySimulation = replayCtx.getBean(AntSimulationImpl.class);
      replaySimulation.setState(simulation.getState());
      simulation.setState(new MemorySimulationDataStore(initialState));

      assertModelsEqual(simulation.getState().getModel(), replaySimulation.getState().getModel());
      for (int cnt = 0; cnt < smallTestIterationCount(); cnt++) {
        replaySimulation.step();
        simulation.step();

        SimulationModel replayModel = replaySimulation.getState().getModel();
        SimulationModel expectedModel = simulation.getState().getModel();

        // The replay simulator random number generator will be out of sync by the end of the
        // iteration so the next seed will be incorrect. The seed is set here so that it won't cause
        // the comparison to fail.
        replayModel.setSeed(expectedModel.getSeed());

        assertModelsEqual(expectedModel, replayModel);
      }
    }
  }

  private void verifyEntityTypeCounts(SimulationModel model, EntityTypeCountObserver observer) {
    EntityTypeCounter counter = new EntityTypeCounter();

    for (EntityModel entity : model.getEnvironment().getEntities()) {
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
