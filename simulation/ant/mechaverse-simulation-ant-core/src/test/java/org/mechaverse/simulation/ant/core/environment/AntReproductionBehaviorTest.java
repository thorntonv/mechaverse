package org.mechaverse.simulation.ant.core.environment;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.genetic.GeneticRecombinator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link AntReproductionBehavior}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AntReproductionBehaviorTest {

  // TODO(thorntonv): Add test to ensure that only ants of reproductive age reproduce.

  @Mock private Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> mockEnv;
  @Mock private GeneticRecombinator mockGeneticRecombinator;

  private AntReproductionBehavior reproductionBehavior;
  private RandomGenerator random;

  @Before
  public void setUp() {
    reproductionBehavior = new AntReproductionBehavior(mockGeneticRecombinator);
    random = RandomUtil.newGenerator(AntReproductionBehaviorTest.class.getName().hashCode());
  }

  @Test
  public void beforeUpdate_antMaxCount() {
    AntSimulationModel state = new AntSimulationModel();
    CellEnvironment env = newEnvironment();
    Ant ant = reproductionBehavior.generateRandomAnt(state, random);
    reproductionBehavior.onAddEntity(ant, state, env);
    reproductionBehavior.setAntMaxCount(1);
    reproductionBehavior.beforeUpdate(state, mockEnv, random);

    verify(mockEnv, never()).addEntity(anyObject());
    verifyZeroInteractions(mockGeneticRecombinator);
  }

  @Test
  public void beforeUpdate_generateRandomAnt() {
    AntSimulationModel state = new AntSimulationModel();
    CellEnvironment env = newEnvironment();
    when(mockEnv.getModel()).thenReturn(env);
    reproductionBehavior.setAntMaxCount(5);
    reproductionBehavior.beforeUpdate(state, mockEnv, random);

    ArgumentCaptor<Ant> antCaptor = ArgumentCaptor.forClass(Ant.class);
    verify(mockEnv).addEntity(antCaptor.capture());
    assertNotNull(env.getCell(antCaptor.getValue()));
    verifyZeroInteractions(mockGeneticRecombinator);
  }

  @Test
  public void beforeUpdate_generateAnt() {
    AntSimulationModel state = new AntSimulationModel();
    CellEnvironment env = newEnvironment();
    when(mockEnv.getModel()).thenReturn(env);
    reproductionBehavior.setAntMaxCount(5);

    Ant parent1 = reproductionBehavior.generateRandomAnt(state, random);
    GeneticDataStore parent1GeneticData =
        randomGeneticDataStore(new GeneticDataStore(parent1));

    reproductionBehavior.onAddEntity(parent1, state, env);

    Ant parent2 = reproductionBehavior.generateRandomAnt(state, random);
    GeneticDataStore parent2GeneticData =
        randomGeneticDataStore(new GeneticDataStore(parent2));
    reproductionBehavior.onAddEntity(parent2, state, env);

    GeneticDataStore childGeneticData = randomGeneticDataStore(new GeneticDataStore(new Ant()));

    when(mockGeneticRecombinator.recombine(
        any(GeneticData.class), any(GeneticData.class), eq(random)))
            .thenReturn(childGeneticData.get(CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY));

    reproductionBehavior.beforeUpdate(state, mockEnv, random);

    ArgumentCaptor<Ant> childCaptor = ArgumentCaptor.forClass(Ant.class);
    verify(mockEnv).addEntity(childCaptor.capture());

    Ant child = childCaptor.getValue();
    assertNotNull(child);

    // Verify that the child genetic data was added to the state.
    assertArrayEquals(child.getData("geneticData." + CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY + ".data"),
        childGeneticData.get(CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY).getData());

    // Verify that recombine was called with the genetic data of the parent ants.
    ArgumentCaptor<GeneticData> geneticDataCaptor1 = ArgumentCaptor.forClass(GeneticData.class);
    ArgumentCaptor<GeneticData> geneticDataCaptor2 = ArgumentCaptor.forClass(GeneticData.class);
    verify(mockGeneticRecombinator).recombine(geneticDataCaptor1.capture(),
        geneticDataCaptor2.capture(), eq(random));
    if (Arrays.equals(geneticDataCaptor1.getValue().getData(),
        parent1GeneticData.get(CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY).getData())) {
      assertArrayEquals(parent2GeneticData.get(CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY).getData(),
          geneticDataCaptor2.getValue().getData());
    } else {
      assertArrayEquals(parent1GeneticData.get(CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY).getData(),
        geneticDataCaptor2.getValue().getData());
      assertArrayEquals(parent2GeneticData.get(CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY).getData(),
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
    reproductionBehavior.onAddEntity(nest, new AntSimulationModel(), env);
    return env;
  }

  private GeneticDataStore randomGeneticDataStore(GeneticDataStore geneticData) {
    for(String type : CellularAutomatonGeneticDataGenerator.KEY_SET) {
      geneticData.put(type, new GeneticData(RandomUtil.randomBytes(100, random),
          ArrayUtil.toIntArray(RandomUtil.randomBytes(400, random)),
          ArrayUtil.toIntArray(RandomUtil.randomBytes(24, random))));
    }
    return geneticData;
  }
}
