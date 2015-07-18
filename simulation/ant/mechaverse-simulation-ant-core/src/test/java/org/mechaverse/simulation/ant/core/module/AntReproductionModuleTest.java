package org.mechaverse.simulation.ant.core.module;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mechaverse.simulation.ant.core.AntSimulationTestUtil.assertEntitiesEqual;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.Nest;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;
import org.mechaverse.simulation.ant.core.entity.EntityDataInputStream;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.genetic.GeneticRecombinator;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
/**
 * Unit test for {@link AntReproductionModule}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AntReproductionModuleTest {

  // TODO(thorntonv): Add test to ensure that only ants of reproductive age reproduce.

  private static final String TEST_GENETIC_DATA_KEY = "testKey";

  @Mock private EntityManager mockEntityManager;
  @Mock private GeneticRecombinator mockGeneticRecombinator;

  private AntReproductionModule module;
  private RandomGenerator random;

  @Before
  public void setUp() {
    module = new AntReproductionModule(mockGeneticRecombinator);
    random = RandomUtil.newGenerator(AntReproductionModuleTest.class.getName().hashCode());
  }

  @Test
  public void beforeUpdate_antMaxCount() {
    AntSimulationState state = new AntSimulationState();
    CellEnvironment env = newEnvironment();
    Ant ant = module.generateRandomAnt(state, random);
    module.onAddEntity(ant, state);
    module.setAntMaxCount(1);
    module.beforeUpdate(state, env, mockEntityManager, random);

    verifyZeroInteractions(mockEntityManager);
    verifyZeroInteractions(mockGeneticRecombinator);
  }

  @Test
  public void beforeUpdate_generateRandomAnt() {
    AntSimulationState state = new AntSimulationState();
    CellEnvironment env = newEnvironment();
    module.setAntMaxCount(5);
    module.beforeUpdate(state, env, mockEntityManager, random);

    ArgumentCaptor<Ant> antCaptor = ArgumentCaptor.forClass(Ant.class);
    verify(mockEntityManager).addEntity(antCaptor.capture());
    assertNotNull(env.getCell(antCaptor.getValue()));
    verifyZeroInteractions(mockGeneticRecombinator);
  }

  @Test
  public void beforeUpdate_generateAnt() throws IOException {
    AntSimulationState state = new AntSimulationState();
    CellEnvironment env = newEnvironment();
    module.setAntMaxCount(5);

    Ant parent1 = module.generateRandomAnt(state, random);
    GeneticDataStore parent1GeneticData =
        randomGeneticDataStore(state.getEntityGeneticDataStore(parent1));

    module.onAddEntity(parent1, state);

    Ant parent2 = module.generateRandomAnt(state, random);
    GeneticDataStore parent2GeneticData =
        randomGeneticDataStore(state.getEntityGeneticDataStore(parent2));
    module.onAddEntity(parent2, state);

    GeneticDataStore childGeneticData = randomGeneticDataStore(new GeneticDataStore());

    when(mockGeneticRecombinator.recombine(
        any(GeneticData.class), any(GeneticData.class), eq(random)))
            .thenReturn(childGeneticData.get(TEST_GENETIC_DATA_KEY));

    module.beforeUpdate(state, env, mockEntityManager, random);

    ArgumentCaptor<Ant> childCaptor = ArgumentCaptor.forClass(Ant.class);
    verify(mockEntityManager).addEntity(childCaptor.capture());

    Ant child = childCaptor.getValue();
    assertNotNull(child);

    // Verify that the child genetic data was added to the state.
    assertEquals(state.getEntityGeneticDataStore(child).get(TEST_GENETIC_DATA_KEY),
        childGeneticData.get(TEST_GENETIC_DATA_KEY));

    // Verify that recombine was called with the genetic data of the parent ants.
    ArgumentCaptor<GeneticData> geneticDataCaptor1 = ArgumentCaptor.forClass(GeneticData.class);
    ArgumentCaptor<GeneticData> geneticDataCaptor2 = ArgumentCaptor.forClass(GeneticData.class);
    verify(mockGeneticRecombinator).recombine(geneticDataCaptor1.capture(),
        geneticDataCaptor2.capture(), eq(random));
    if (Arrays.equals(geneticDataCaptor1.getValue().getData(),
        parent1GeneticData.get(TEST_GENETIC_DATA_KEY).getData())) {
      assertArrayEquals(parent2GeneticData.get(TEST_GENETIC_DATA_KEY).getData(),
          geneticDataCaptor2.getValue().getData());
    } else {
      assertArrayEquals(parent1GeneticData.get(TEST_GENETIC_DATA_KEY).getData(),
        geneticDataCaptor2.getValue().getData());
      assertArrayEquals(parent2GeneticData.get(TEST_GENETIC_DATA_KEY).getData(),
        geneticDataCaptor1.getValue().getData());
    }
  }

  @Test
  public void recordAntGenerationReplayData() throws IOException {
    AntSimulationState state = new AntSimulationState();
    CellEnvironment env = newEnvironment();
    module.setAntMaxCount(5);
    module.setState(state, env, mockEntityManager);
    state.getModel().setIteration(10);
    module.beforeUpdate(state, env, mockEntityManager, random);
    state.getModel().setIteration(18);
    module.beforeUpdate(state, env, mockEntityManager, random);

    ArgumentCaptor<Ant> antCaptor = ArgumentCaptor.forClass(Ant.class);
    verify(mockEntityManager, times(2)).addEntity(antCaptor.capture());
    assertNotNull(env.getCell(antCaptor.getValue()));
    verifyZeroInteractions(mockGeneticRecombinator);

    module.updateState(state, env, mockEntityManager);

    try(EntityDataInputStream replayDataIn = new EntityDataInputStream(new ByteArrayInputStream(
        state.getReplayDataStore().get(AntReproductionReplayModule.ANT_GENERATION_DATA_KEY)))) {
      assertEquals(10, replayDataIn.readLong());
      assertEquals(1, replayDataIn.readInt());
      assertEntitiesEqual(antCaptor.getAllValues().get(0), replayDataIn.readEntity());
      assertEquals(18, replayDataIn.readLong());
      assertEquals(1, replayDataIn.readInt());
      assertEntitiesEqual(antCaptor.getAllValues().get(1), replayDataIn.readEntity());
    }
  }

  private CellEnvironment newEnvironment() {
    Environment env = new Environment();
    env.setWidth(40);
    env.setHeight(40);
    Nest nest = new Nest();
    nest.setX(20);
    nest.setY(20);
    env.getEntities().add(nest);
    module.onAddEntity(nest, new AntSimulationState());
    return new CellEnvironment(env);
  }

  private GeneticDataStore randomGeneticDataStore(GeneticDataStore geneticData) throws IOException {
    geneticData.put(TEST_GENETIC_DATA_KEY, new GeneticData(RandomUtil.randomBytes(100, random),
        ArrayUtil.toIntArray(RandomUtil.randomBytes(400, random)),
            ArrayUtil.toIntArray(RandomUtil.randomBytes(24, random))));
    return geneticData;
  }
}
