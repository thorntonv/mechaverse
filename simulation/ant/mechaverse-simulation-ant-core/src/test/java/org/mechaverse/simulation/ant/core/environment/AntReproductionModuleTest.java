package org.mechaverse.simulation.ant.core.environment;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.model.Nest;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.genetic.GeneticRecombinator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link AntReproductionBehavior}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AntReproductionModuleTest {

  // TODO(thorntonv): Add test to ensure that only ants of reproductive age reproduce.

  private static final String TEST_GENETIC_DATA_KEY = "testKey";

  @Mock private Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> mockEnv;
  @Mock private GeneticRecombinator mockGeneticRecombinator;

  private AntReproductionBehavior module;
  private RandomGenerator random;

  @Before
  public void setUp() {
    module = new AntReproductionBehavior(mockGeneticRecombinator);
    random = RandomUtil.newGenerator(AntReproductionModuleTest.class.getName().hashCode());
  }

  @Test
  public void beforeUpdate_antMaxCount() {
    AntSimulationModel state = new AntSimulationModel();
    CellEnvironment env = newEnvironment();
    Ant ant = module.generateRandomAnt(state, random);
    module.onAddEntity(ant, state, env);
    module.setAntMaxCount(1);
    module.beforeUpdate(state, mockEnv, random);

    verifyZeroInteractions(mockEnv);
    verifyZeroInteractions(mockGeneticRecombinator);
  }

  @Test
  public void beforeUpdate_generateRandomAnt() {
    AntSimulationModel state = new AntSimulationModel();
    CellEnvironment env = newEnvironment();
    module.setAntMaxCount(5);
    module.beforeUpdate(state, mockEnv, random);

    ArgumentCaptor<Ant> antCaptor = ArgumentCaptor.forClass(Ant.class);
    verify(mockEnv).addEntity(antCaptor.capture());
    assertNotNull(env.getCell(antCaptor.getValue()));
    verifyZeroInteractions(mockGeneticRecombinator);
  }

  @Test
  public void beforeUpdate_generateAnt() {
    AntSimulationModel state = new AntSimulationModel();
    CellEnvironment env = newEnvironment();
    module.setAntMaxCount(5);

    Ant parent1 = module.generateRandomAnt(state, random);
    GeneticDataStore parent1GeneticData =
        randomGeneticDataStore(new GeneticDataStore(parent1));

    module.onAddEntity(parent1, state, env);

    Ant parent2 = module.generateRandomAnt(state, random);
    GeneticDataStore parent2GeneticData =
        randomGeneticDataStore(new GeneticDataStore(parent2));
    module.onAddEntity(parent2, state, env);

    GeneticDataStore childGeneticData = randomGeneticDataStore(new GeneticDataStore(new Ant()));

    when(mockGeneticRecombinator.recombine(
        any(GeneticData.class), any(GeneticData.class), eq(random)))
            .thenReturn(childGeneticData.get(TEST_GENETIC_DATA_KEY));

    module.beforeUpdate(state, mockEnv, random);

    ArgumentCaptor<Ant> childCaptor = ArgumentCaptor.forClass(Ant.class);
    verify(mockEnv).addEntity(childCaptor.capture());

    Ant child = childCaptor.getValue();
    assertNotNull(child);

    // Verify that the child genetic data was added to the state.
    assertEquals(child.getData(TEST_GENETIC_DATA_KEY),
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

  private CellEnvironment newEnvironment() {
    CellEnvironment env = new CellEnvironment();
    env.setWidth(40);
    env.setHeight(40);
    Nest nest = new Nest();
    nest.setX(20);
    nest.setY(20);
    env.getEntities().add(nest);
    module.onAddEntity(nest, new AntSimulationModel(), env);
    return env;
  }

  private GeneticDataStore randomGeneticDataStore(GeneticDataStore geneticData) {
    geneticData.put(TEST_GENETIC_DATA_KEY, new GeneticData(RandomUtil.randomBytes(100, random),
        ArrayUtil.toIntArray(RandomUtil.randomBytes(400, random)),
            ArrayUtil.toIntArray(RandomUtil.randomBytes(24, random))));
    return geneticData;
  }
}
