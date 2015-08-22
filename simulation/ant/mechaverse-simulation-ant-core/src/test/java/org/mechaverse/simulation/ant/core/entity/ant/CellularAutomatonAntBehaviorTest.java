package org.mechaverse.simulation.ant.core.entity.ant;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.common.SimulationStateCellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonAllocator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulationUtil;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableSet;

/**
 * Unit test for {@link CellularAutomatonAntBehavior}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellularAutomatonAntBehaviorTest {

  private static final int TEST_AUTOMATON_STATE_SIZE = 100;
  private static final int TEST_AUTOMATON_STATE_SIZE_BYTES =
      CellularAutomatonSimulationUtil.stateSizeInBytes(TEST_AUTOMATON_STATE_SIZE);
  private static final int TEST_AUTOMATON_OUTPUT_SIZE = 32;
  private static final int TEST_AUTOMATON_OUTPUT_SIZE_BYTES =
      CellularAutomatonSimulationUtil.outputSizeInBytes(TEST_AUTOMATON_OUTPUT_SIZE);

  @Mock private CellularAutomatonSimulator mockSimulator;
  @Mock private CellularAutomatonAllocator mockAllocator;

  private Ant ant;
  private CellularAutomatonAntBehavior behavior;
  private AntInput input;
  private RandomGenerator random;
  private AntSimulationState state;
  private CellularAutomatonDescriptorDataSource descriptorDataSource =
      new SimulationStateCellularAutomatonDescriptor();

  @Before
  public void setUp() {
    when(mockSimulator.getAllocator()).thenReturn(mockAllocator);
    when(mockAllocator.allocate()).thenReturn(0);
    when(mockSimulator.getAutomatonStateSize()).thenReturn(TEST_AUTOMATON_STATE_SIZE);
    when(mockSimulator.getAutomatonOutputSize()).thenReturn(TEST_AUTOMATON_OUTPUT_SIZE);
    input = new AntInput();
    random = RandomUtil.newGenerator(CellularAutomatonAntBehavior.class.getName().hashCode());
    behavior = new CellularAutomatonAntBehavior(descriptorDataSource, mockSimulator);
    state = new AntSimulationState();

    ant = new Ant();
    ant.setId("001");
    behavior.setEntity(ant);
  }

  @Test
  public void generateGeneticData() throws IOException {
    behavior.setState(state);
    behavior.setInput(input, random);

    // Verify that the cellular automaton state was initialized.

    SimulationDataStore entityDataStore = state.getEntityDataStore(ant);

    byte[] automatonState = entityDataStore.get(CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY);
    byte[] outputMap = entityDataStore.get(CellularAutomatonAntBehavior.AUTOMATON_OUTPUT_MAP_KEY);
    byte[] bitOutputMap = entityDataStore.get(CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY);

    CellularAutomatonSimulationModel model = descriptorDataSource.getSimulationModel();
    assertEquals(
      CellularAutomatonSimulationUtil.stateSizeInBytes(model.getStateSize()), automatonState.length);
    assertEquals(TEST_AUTOMATON_OUTPUT_SIZE_BYTES, outputMap.length);
    assertEquals(TEST_AUTOMATON_OUTPUT_SIZE_BYTES, bitOutputMap.length);

    // Verify that the genetic data was stored.
    GeneticDataStore geneticData = state.getEntityGeneticDataStore(ant);
    assertEquals(ImmutableSet.of(CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_KEY,
        CellularAutomatonGeneticDataGenerator.OUTPUT_MAP_KEY,
            CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY), geneticData.keySet());

    // Verify that the stored genetic data matches the initial cellular automaton state.

    GeneticData automatonStateData =
        geneticData.get(CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_KEY);
    assertArrayEquals(automatonState, automatonStateData.getData());
    assertNotNull(automatonStateData.getCrossoverGroups());
    assertNotNull(automatonStateData.getCrossoverSplitPoints());

    GeneticData outputMapData =
        geneticData.get(CellularAutomatonGeneticDataGenerator.OUTPUT_MAP_KEY);
    assertArrayEquals(outputMap, outputMapData.getData());
    assertNotNull(outputMapData.getCrossoverGroups());
    assertNotNull(outputMapData.getCrossoverSplitPoints());

    GeneticData bitOutputMapData =
        geneticData.get(CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY);
    assertArrayEquals(bitOutputMap, bitOutputMapData.getData());
    assertNotNull(bitOutputMapData.getCrossoverGroups());
    assertNotNull(bitOutputMapData.getCrossoverSplitPoints());
  }

  @Test
  public void loadGeneticData() throws IOException {
    createGeneticDataStore(state.getEntityGeneticDataStore(ant));
    behavior.setState(state);
    behavior.setInput(input, random);

    // Verify that the initial state was stored.
    SimulationDataStore entityDataStore = state.getEntityDataStore(ant);
    GeneticDataStore entityGeneticDataStore = state.getEntityGeneticDataStore(ant);

    byte[] expectedState = entityGeneticDataStore
        .get(CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_KEY).getData();
    byte[] expectedOutputMap = entityGeneticDataStore
        .get(CellularAutomatonGeneticDataGenerator.OUTPUT_MAP_KEY).getData();

    assertArrayEquals(
        expectedState, entityDataStore.get(CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY));
    assertArrayEquals(expectedOutputMap,
        entityDataStore.get(CellularAutomatonAntBehavior.AUTOMATON_OUTPUT_MAP_KEY));
    assertArrayEquals(entityGeneticDataStore.get(
        CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY).getData(),
            entityDataStore.get(CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY));

    // Verify that the cellular automaton simulator was initialized.

    verify(mockSimulator).setAutomatonState(eq(0), eq(ArrayUtil.toIntArray(expectedState)));
    verify(mockSimulator).setAutomatonOutputMap(eq(0), eq(ArrayUtil.toIntArray(expectedOutputMap)));
  }

  @Test
  public void setInput() {
    input = new AntInput(
        ArrayUtil.toIntArray(RandomUtil.randomBytes(AntInput.DATA_SIZE * 4, random)));
    behavior.setState(state);
    behavior.setInput(input, random);
    verify(mockSimulator).setAutomatonInput(0, input.getData());
  }

  @Test
  public void getOutput() throws IOException {
    GeneticDataStore geneticData = state.getEntityGeneticDataStore(ant);
    createGeneticDataStore(geneticData);

    final int[] automatonOutput = ArrayUtil.toIntArray(
        RandomUtil.randomBytes(TEST_AUTOMATON_OUTPUT_SIZE_BYTES, random));
    final ArgumentCaptor<int[]> outputCaptor = ArgumentCaptor.forClass(int[].class);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        for (int idx = 0; idx < automatonOutput.length; idx++) {
          outputCaptor.getValue()[idx] = automatonOutput[idx];
        }
        return null;
      }
    }).when(mockSimulator).getAutomatonOutput(eq(0), outputCaptor.capture());
    behavior.setState(state);
    behavior.setInput(input, random);
    int[] antOutputData = behavior.getOutput(random).getData();
    assertEquals(AntOutput.DATA_SIZE, antOutputData.length);

    int[] bitOutputMap = ArrayUtil.toIntArray(
        geneticData.get(CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY).getData());
    for (int idx = 0; idx < automatonOutput.length; idx++) {
      assertEquals(((automatonOutput[idx] >> bitOutputMap[idx]) & 1), antOutputData[0] & 1);
      antOutputData[0] >>= 1;
    }
  }

  @Test
  public void onRemoveEntity() {
    Entity otherEntity = new Entity();
    otherEntity.setId("100");
    createGeneticDataStore(state.getEntityGeneticDataStore(otherEntity));

    Set<String> expectedKeySet = new HashSet<>(state.keySet());

    GeneticDataStore geneticData = state.getEntityGeneticDataStore(ant);
    createGeneticDataStore(geneticData);

    behavior.setState(state);
    behavior.setInput(input, random);

    behavior.onRemoveEntity();

    verify(mockAllocator).deallocate(0);
    assertEquals(expectedKeySet, state.keySet());
  }

  @Test
  public void setState() throws IOException {
    byte[] stateBytes = RandomUtil.randomBytes(TEST_AUTOMATON_STATE_SIZE_BYTES, random);
    byte[] outputMapBytes = RandomUtil.randomBytes(TEST_AUTOMATON_OUTPUT_SIZE_BYTES, random);

    SimulationDataStore dataStore = state.getEntityDataStore(ant);
    dataStore.put(CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY, stateBytes);
    dataStore.put(CellularAutomatonAntBehavior.AUTOMATON_OUTPUT_MAP_KEY, outputMapBytes);

    behavior.setState(state);

    verify(mockSimulator).setAutomatonState(eq(0), eq(ArrayUtil.toIntArray(stateBytes)));
    verify(mockSimulator).setAutomatonOutputMap(eq(0), eq(ArrayUtil.toIntArray(outputMapBytes)));
  }

  @Test
  public void updateState() throws IOException {
    final byte[] stateBytes =
        RandomUtil.randomBytes(TEST_AUTOMATON_STATE_SIZE_BYTES, random);
    SimulationDataStore entityDataStore = state.getEntityDataStore(ant);
    entityDataStore.put(CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY, new byte[0]);

    final ArgumentCaptor<int[]> stateCaptor = ArgumentCaptor.forClass(int[].class);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        int[] automatonState = ArrayUtil.toIntArray(stateBytes);
        for (int idx = 0; idx < automatonState.length; idx++) {
          stateCaptor.getValue()[idx] = automatonState[idx];
        }
        return null;
      }
    }).when(mockSimulator).getAutomatonState(eq(0), stateCaptor.capture());

    behavior.setState(state);
    behavior.updateState(state);
    assertArrayEquals(stateBytes,
        entityDataStore.get(CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY));
  }

  private void createGeneticDataStore(GeneticDataStore geneticDataStore) {
    GeneticData stateData = new GeneticData(
        RandomUtil.randomBytes(TEST_AUTOMATON_STATE_SIZE_BYTES, random),
            new int[TEST_AUTOMATON_STATE_SIZE_BYTES], new int[0]);
    geneticDataStore.put(
        CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_KEY, stateData);

    GeneticData outputMapData = new GeneticData(
        RandomUtil.randomBytes(TEST_AUTOMATON_OUTPUT_SIZE_BYTES, random),
            new int[TEST_AUTOMATON_OUTPUT_SIZE_BYTES], new int[0]);
    geneticDataStore.put(CellularAutomatonGeneticDataGenerator.OUTPUT_MAP_KEY, outputMapData);

    int[] bitOutputMap = new int[TEST_AUTOMATON_OUTPUT_SIZE];
    for (int idx = 0; idx < bitOutputMap.length; idx++) {
      bitOutputMap[idx] = random.nextInt(Integer.SIZE);
    }
    byte[] bitOutputMapBytes = ArrayUtil.toByteArray(bitOutputMap);
    GeneticData bitOutputMapData =
        new GeneticData(bitOutputMapBytes, new int[bitOutputMapBytes.length], new int[0]);
    geneticDataStore.put(CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY,
        bitOutputMapData);
 }
}
