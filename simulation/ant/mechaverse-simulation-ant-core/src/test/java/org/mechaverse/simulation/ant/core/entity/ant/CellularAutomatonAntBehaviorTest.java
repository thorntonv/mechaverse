package org.mechaverse.simulation.ant.core.entity.ant;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.SimulationStateCellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonAllocator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulationUtil;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
  private AntSimulationModel state;
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
    state = new AntSimulationModel();

    ant = new Ant();
    ant.setId("001");
    behavior.setModel(ant);
  }

  @Test
  public void generateGeneticData() {
    behavior.setState(state);
    behavior.setInput(input, random);

    // Verify that the cellular automaton state was initialized.

    byte[] automatonState = ant.getData(CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY);
    byte[] outputMap = ant.getData(CellularAutomatonAntBehavior.AUTOMATON_OUTPUT_MAP_KEY);
    byte[] bitOutputMap = ant.getData(CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY);

    CellularAutomatonSimulationModel model = descriptorDataSource.getSimulationModel();
    assertEquals(
      CellularAutomatonSimulationUtil.stateSizeInBytes(model.getStateSize()), automatonState.length);
    assertEquals(TEST_AUTOMATON_OUTPUT_SIZE_BYTES, outputMap.length);
    assertEquals(TEST_AUTOMATON_OUTPUT_SIZE_BYTES, bitOutputMap.length);

    // Verify that the genetic data was stored.
    GeneticDataStore geneticData = new GeneticDataStore(ant);
    assertTrue(geneticData.contains(CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY));
    assertTrue(geneticData.contains(CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_OUTPUT_MAP_GENETIC_DATA_KEY));
    assertTrue(geneticData.contains(CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY));

    // Verify that the stored genetic data matches the initial cellular automaton state.

    GeneticData automatonStateData =
        geneticData.get(CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY);
    assertArrayEquals(automatonState, automatonStateData.getData());
    assertNotNull(automatonStateData.getCrossoverGroups());
    assertNotNull(automatonStateData.getCrossoverSplitPoints());

    GeneticData outputMapData =
        geneticData.get(CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_OUTPUT_MAP_GENETIC_DATA_KEY);
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
  public void loadGeneticData() {
    createGeneticDataStore(new GeneticDataStore(ant));
    behavior.setState(state);
    behavior.setInput(input, random);

    // Verify that the initial state was stored.
    GeneticDataStore entityGeneticDataStore = new GeneticDataStore(ant);

    byte[] expectedState = entityGeneticDataStore
        .get(CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY).getData();
    byte[] expectedOutputMap = entityGeneticDataStore
        .get(CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_OUTPUT_MAP_GENETIC_DATA_KEY).getData();

    assertArrayEquals(
        expectedState, ant.getData(CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY));
    assertArrayEquals(expectedOutputMap,
        ant.getData(CellularAutomatonAntBehavior.AUTOMATON_OUTPUT_MAP_KEY));
    assertArrayEquals(entityGeneticDataStore.get(
        CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY).getData(),
            ant.getData(CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY));

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
  public void getOutput() {
    GeneticDataStore geneticData = new GeneticDataStore(ant);
    createGeneticDataStore(geneticData);

    final int[] automatonOutput = ArrayUtil.toIntArray(
        RandomUtil.randomBytes(TEST_AUTOMATON_OUTPUT_SIZE_BYTES, random));
    final ArgumentCaptor<int[]> outputCaptor = ArgumentCaptor.forClass(int[].class);
    doAnswer((Answer<Void>) invocation -> {
      for (int idx = 0; idx < automatonOutput.length; idx++) {
        outputCaptor.getValue()[idx] = automatonOutput[idx];
      }
      return null;
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
    EntityModel otherEntity = new Ant();
    otherEntity.setId("100");
    createGeneticDataStore(new GeneticDataStore(otherEntity));

    // Set<String> expectedKeySet = new HashSet<>(state.keySet());

    GeneticDataStore geneticData = new GeneticDataStore(ant);
    createGeneticDataStore(geneticData);

    behavior.setState(state);
    behavior.setInput(input, random);

    behavior.onRemoveEntity();

    verify(mockAllocator).deallocate(0);
    // assertEquals(expectedKeySet, state.keySet());
  }

  @Test
  public void setState() {
    byte[] stateBytes = RandomUtil.randomBytes(TEST_AUTOMATON_STATE_SIZE_BYTES, random);
    byte[] outputMapBytes = RandomUtil.randomBytes(TEST_AUTOMATON_OUTPUT_SIZE_BYTES, random);

    ant.putData(CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY, stateBytes);
    ant.putData(CellularAutomatonAntBehavior.AUTOMATON_OUTPUT_MAP_KEY, outputMapBytes);

    behavior.setState(state);

    verify(mockSimulator).setAutomatonState(eq(0), eq(ArrayUtil.toIntArray(stateBytes)));
    verify(mockSimulator).setAutomatonOutputMap(eq(0), eq(ArrayUtil.toIntArray(outputMapBytes)));
  }

  @Test
  public void updateState() {
    final byte[] stateBytes =
        RandomUtil.randomBytes(TEST_AUTOMATON_STATE_SIZE_BYTES, random);
    ant.putData(CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY, new byte[0]);

    final ArgumentCaptor<int[]> stateCaptor = ArgumentCaptor.forClass(int[].class);
    doAnswer((Answer<Void>) invocation -> {
      int[] automatonState = ArrayUtil.toIntArray(stateBytes);
      for (int idx = 0; idx < automatonState.length; idx++) {
        stateCaptor.getValue()[idx] = automatonState[idx];
      }
      return null;
    }).when(mockSimulator).getAutomatonState(eq(0), stateCaptor.capture());

    behavior.setState(state);
    behavior.updateState(state);
    assertArrayEquals(stateBytes,
        ant.getData(CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY));
  }

  private void createGeneticDataStore(GeneticDataStore geneticDataStore) {
    GeneticData stateData = new GeneticData(
        RandomUtil.randomBytes(TEST_AUTOMATON_STATE_SIZE_BYTES, random),
            new int[TEST_AUTOMATON_STATE_SIZE_BYTES], new int[0]);
    geneticDataStore.put(
        CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY, stateData);

    GeneticData outputMapData = new GeneticData(
        RandomUtil.randomBytes(TEST_AUTOMATON_OUTPUT_SIZE_BYTES, random),
            new int[TEST_AUTOMATON_OUTPUT_SIZE_BYTES], new int[0]);
    geneticDataStore.put(CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_OUTPUT_MAP_GENETIC_DATA_KEY, outputMapData);

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
