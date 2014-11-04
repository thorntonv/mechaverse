package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mechaverse.simulation.common.datastore.SimulationDataStoreOutputStream.toByteArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.SimulationModelUtil;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.ant.api.util.EntityUtil;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.io.ByteStreams;

/**
 * Unit test for {@link AntSimulationImpl}.
 */
public abstract class AbstractAntSimulationImplTest {

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
    public void onAddEntity(Entity entity, AntSimulationState state) {
      entityTypeCounter.addEntity(entity);
    }

    @Override
    public void onRemoveEntity(Entity entity, AntSimulationState state) {
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

    for (int cnt = 0; cnt < 50; cnt++) {
      simulation1.step();
      simulation2.step();

      verifyStatesEqual(simulation1.getState(), simulation2.getState());
    }

    byte[] state = toByteArray(simulation1.getState());
    simulation1 = newSimulationImpl();
    simulation1.setState(MemorySimulationDataStore.fromByteArray(state));
    simulation2 = newSimulationImpl();
    simulation2.setState(MemorySimulationDataStore.fromByteArray(state));

    for (int cnt = 0; cnt < 50; cnt++) {
      simulation1.step();
      simulation2.step();

      verifyStatesEqual(simulation1.getState(), simulation2.getState());
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
    for (int cnt = 0; cnt < testIterationCount(); cnt++) {
      simulation.step();
    }

    try(ClassPathXmlApplicationContext replayCtx =
        new ClassPathXmlApplicationContext("test-simulation-context-replay.xml")) {
      AntSimulationImpl replaySimulation = replayCtx.getBean(AntSimulationImpl.class);
      replaySimulation.setState(simulation.getState());
      simulation.setState(new MemorySimulationDataStore(initialState));
      
      assertModelsEqual(simulation.getState().getModel(), replaySimulation.getState().getModel());
      for (int cnt = 0; cnt < testIterationCount(); cnt++) {
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

  private void verifyStatesEqual(AntSimulationState state1, AntSimulationState state2)
      throws IOException {
    assertEquals(state1.getModel().getSeed(), state2.getModel().getSeed());
    assertEquals(state1.getModel().getEnvironment().getEntities().size(),
        state2.getModel().getEnvironment().getEntities().size());

    assertTrue(state1.keySet().toString(), state1.keySet().contains(AntSimulationState.MODEL_KEY));
    assertEquals(state1.keySet(), state2.keySet());
    for (String key : state1.keySet()) {
      byte[] data1 = state1.get(key);
      byte[] data2 = state2.get(key);
      if (key.equalsIgnoreCase(AntSimulationState.MODEL_KEY)) {
        data1 = decompress(data1);
        data2 = decompress(data2);
      }

      assertArrayEquals("Data for key " + key + " does not match.", data1, data2);
    }
  }

  // TODO(thorntonv): Move this to a common test utility class.
  private void assertModelsEqual(SimulationModel expected, SimulationModel actual)
      throws IOException {
    assertEquals(new CellEnvironment(expected.getEnvironment()).toString(),
      new CellEnvironment(actual.getEnvironment()).toString());

    // Sort the entities so that order will not cause the comparison to fail.
    for (Environment env : SimulationModelUtil.getEnvironments(expected)) {
      Collections.sort(env.getEntities(), EntityUtil.ENTITY_ORDERING);
    }
    for (Environment env : SimulationModelUtil.getEnvironments(actual)) {
      Collections.sort(env.getEntities(), EntityUtil.ENTITY_ORDERING);
    }

    ByteArrayOutputStream model1ByteOut = new ByteArrayOutputStream(16 * 1024);
    ByteArrayOutputStream model2ByteOut = new ByteArrayOutputStream(16 * 1024);

    SimulationModelUtil.serialize(expected, model1ByteOut);
    SimulationModelUtil.serialize(actual, model2ByteOut);

    assertEquals(model1ByteOut.toString(), model2ByteOut.toString());
  }

  private byte[] decompress(byte[] data) throws IOException {
    return ByteStreams.toByteArray(new GZIPInputStream(new ByteArrayInputStream(data)));
  }
}
