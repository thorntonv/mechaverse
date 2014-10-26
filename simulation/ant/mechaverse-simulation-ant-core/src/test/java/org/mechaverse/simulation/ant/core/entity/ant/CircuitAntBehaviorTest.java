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
import org.mechaverse.simulation.common.circuit.CircuitAllocator;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.mechaverse.simulation.common.circuit.CircuitUtil;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.genetic.CircuitGeneticDataGenerator;
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
 * Unit test for {@link CircuitAntBehavior}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CircuitAntBehaviorTest {

  private static final int TEST_CIRCUIT_STATE_SIZE = 100;
  private static final int TEST_CIRCUIT_STATE_SIZE_BYTES =
      CircuitUtil.stateSizeInBytes(TEST_CIRCUIT_STATE_SIZE);
  private static final int TEST_CIRCUIT_OUTPUT_SIZE = 32;
  private static final int TEST_CIRCUIT_OUTPUT_SIZE_BYTES =
      CircuitUtil.outputSizeInBytes(TEST_CIRCUIT_OUTPUT_SIZE);

  @Mock private CircuitSimulator mockCircuitSimulator;
  @Mock private CircuitAllocator mockCircuitAllocator;

  private Ant ant;
  private CircuitAntBehavior behavior;
  private AntInput input;
  private RandomGenerator random;
  private AntSimulationState state;

  @Before
  public void setUp() {
    when(mockCircuitSimulator.getAllocator()).thenReturn(mockCircuitAllocator);
    when(mockCircuitAllocator.allocateCircuit()).thenReturn(0);
    when(mockCircuitSimulator.getCircuitStateSize()).thenReturn(TEST_CIRCUIT_STATE_SIZE);
    when(mockCircuitSimulator.getCircuitOutputSize()).thenReturn(TEST_CIRCUIT_OUTPUT_SIZE);
    input = new AntInput();
    random = RandomUtil.newGenerator(CircuitAntBehavior.class.getName().hashCode());
    behavior = new CircuitAntBehavior(mockCircuitSimulator);
    state = new AntSimulationState();

    ant = new Ant();
    ant.setId("001");
    behavior.setEntity(ant);
  }

  @Test
  public void generateGeneticData() throws IOException {
    behavior.setState(state);
    behavior.setInput(input, random);

    // Verify that the circuit state was initialized.

    SimulationDataStore entityDataStore = state.getEntityDataStore(ant);

    byte[] circuitState = entityDataStore.get(CircuitAntBehavior.CIRCUIT_STATE_KEY);
    byte[] outputMap = entityDataStore.get(CircuitAntBehavior.CIRCUIT_OUTPUT_MAP_KEY);
    byte[] bitOutputMap = entityDataStore.get(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY);

    assertEquals(TEST_CIRCUIT_STATE_SIZE_BYTES, circuitState.length);
    assertEquals(TEST_CIRCUIT_OUTPUT_SIZE_BYTES, outputMap.length);
    assertEquals(TEST_CIRCUIT_OUTPUT_SIZE_BYTES, bitOutputMap.length);

    // Verify that the genetic data was stored.
    GeneticDataStore geneticData = state.getEntityGeneticDataStore(ant);
    assertEquals(ImmutableSet.of(CircuitGeneticDataGenerator.CIRCUIT_STATE_KEY,
        CircuitGeneticDataGenerator.OUTPUT_MAP_KEY, CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY),
            geneticData.keySet());

    // Verify that the stored genetic data matches the initial circuit state.

    GeneticData circuitStateData = geneticData.get(CircuitGeneticDataGenerator.CIRCUIT_STATE_KEY);
    assertArrayEquals(circuitState, circuitStateData.getData());
    assertNotNull(circuitStateData.getCrossoverPoints());

    GeneticData outputMapData = geneticData.get(CircuitGeneticDataGenerator.OUTPUT_MAP_KEY);
    assertArrayEquals(outputMap, outputMapData.getData());
    assertNotNull(outputMapData.getCrossoverPoints());

    GeneticData bitOutputMapData = geneticData.get(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY);
    assertArrayEquals(bitOutputMap, bitOutputMapData.getData());
    assertNotNull(bitOutputMapData.getCrossoverPoints());
  }

  @Test
  public void loadGeneticData() throws IOException {
    createGeneticDataStore(state.getEntityGeneticDataStore(ant));
    behavior.setState(state);
    behavior.setInput(input, random);

    // Verify that the initial state was stored.
    SimulationDataStore entityDataStore = state.getEntityDataStore(ant);
    GeneticDataStore entityGeneticDataStore = state.getEntityGeneticDataStore(ant);

    byte[] expectedCircuitState = entityGeneticDataStore
        .get(CircuitGeneticDataGenerator.CIRCUIT_STATE_KEY).getData();
    byte[] expectedOutputMap = entityGeneticDataStore
        .get(CircuitGeneticDataGenerator.OUTPUT_MAP_KEY).getData();

    assertArrayEquals(
        expectedCircuitState, entityDataStore.get(CircuitAntBehavior.CIRCUIT_STATE_KEY));
    assertArrayEquals(
        expectedOutputMap, entityDataStore.get(CircuitAntBehavior.CIRCUIT_OUTPUT_MAP_KEY));
    assertArrayEquals(
        entityGeneticDataStore.get(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY).getData(),
        entityDataStore.get(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY));

    // Verify that the circuit simulator was initialized.

    verify(mockCircuitSimulator).setCircuitState(eq(0),
        eq(ArrayUtil.toIntArray(expectedCircuitState)));
    verify(mockCircuitSimulator).setCircuitOutputMap(eq(0),
        eq(ArrayUtil.toIntArray(expectedOutputMap)));
  }

  @Test
  public void setInput() {
    input = new AntInput(
        ArrayUtil.toIntArray(RandomUtil.randomBytes(AntInput.DATA_SIZE * 4, random)));
    behavior.setState(state);
    behavior.setInput(input, random);
    verify(mockCircuitSimulator).setCircuitInput(0, input.getData());
  }

  @Test
  public void getOutput() throws IOException {
    GeneticDataStore geneticData = state.getEntityGeneticDataStore(ant);
    createGeneticDataStore(geneticData);

    final int[] circuitOutput = ArrayUtil.toIntArray(
        RandomUtil.randomBytes(TEST_CIRCUIT_OUTPUT_SIZE_BYTES, random));
    final ArgumentCaptor<int[]> circuitOutputCaptor = ArgumentCaptor.forClass(int[].class);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        for (int idx = 0; idx < circuitOutput.length; idx++) {
          circuitOutputCaptor.getValue()[idx] = circuitOutput[idx];
        }
        return null;
      }
    }).when(mockCircuitSimulator).getCircuitOutput(eq(0), circuitOutputCaptor.capture());
    behavior.setState(state);
    behavior.setInput(input, random);
    int[] antOutputData = behavior.getOutput(random).getData();
    assertEquals(AntOutput.DATA_SIZE, antOutputData.length);

    int[] bitOutputMap = ArrayUtil.toIntArray(
        geneticData.get(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY).getData());
    for (int idx = 0; idx < circuitOutput.length; idx++) {
      assertEquals(((circuitOutput[idx] >> bitOutputMap[idx]) & 1), antOutputData[0] & 1);
      antOutputData[0] >>= 1;
    }
  }

  @Test
  public void onRemoveEntity() {
    Entity otherEntity = new Entity();
    otherEntity.setId("100");
    createGeneticDataStore(state.getEntityGeneticDataStore(otherEntity));

    Set<String> expectedKeySet = new HashSet<String>(state.keySet());

    GeneticDataStore geneticData = state.getEntityGeneticDataStore(ant);
    createGeneticDataStore(geneticData);

    behavior.setState(state);
    behavior.setInput(input, random);

    behavior.onRemoveEntity();

    verify(mockCircuitAllocator).deallocateCircuit(0);
    assertEquals(expectedKeySet, state.keySet());
  }

  @Test
  public void setState() throws IOException {
    byte[] circuitStateBytes = RandomUtil.randomBytes(TEST_CIRCUIT_STATE_SIZE_BYTES, random);
    byte[] outputMapBytes = RandomUtil.randomBytes(TEST_CIRCUIT_OUTPUT_SIZE_BYTES, random);

    SimulationDataStore dataStore = state.getEntityDataStore(ant);
    dataStore.put(CircuitAntBehavior.CIRCUIT_STATE_KEY, circuitStateBytes);
    dataStore.put(CircuitAntBehavior.CIRCUIT_OUTPUT_MAP_KEY, outputMapBytes);

    behavior.setState(state);

    verify(mockCircuitSimulator)
        .setCircuitState(eq(0), eq(ArrayUtil.toIntArray(circuitStateBytes)));
    verify(mockCircuitSimulator)
        .setCircuitOutputMap(eq(0), eq(ArrayUtil.toIntArray(outputMapBytes)));
  }

  @Test
  public void updateState() throws IOException {
    final byte[] circuitStateBytes = RandomUtil.randomBytes(TEST_CIRCUIT_STATE_SIZE_BYTES, random);
    SimulationDataStore entityDataStore = state.getEntityDataStore(ant);
    entityDataStore.put(CircuitAntBehavior.CIRCUIT_STATE_KEY, new byte[0]);

    final ArgumentCaptor<int[]> circuitStateCaptor = ArgumentCaptor.forClass(int[].class);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        int[] circuitState = ArrayUtil.toIntArray(circuitStateBytes);
        for (int idx = 0; idx < circuitState.length; idx++) {
          circuitStateCaptor.getValue()[idx] = circuitState[idx];
        }
        return null;
      }
    }).when(mockCircuitSimulator).getCircuitState(eq(0), circuitStateCaptor.capture());

    behavior.setState(state);
    behavior.updateState(state);
    assertArrayEquals(circuitStateBytes, entityDataStore.get(CircuitAntBehavior.CIRCUIT_STATE_KEY));
  }

  private void createGeneticDataStore(GeneticDataStore geneticDataStore) {
    GeneticData circuitStateData = new GeneticData(RandomUtil.randomBytes(
        TEST_CIRCUIT_STATE_SIZE_BYTES, random), new int[0]);
    geneticDataStore.put(CircuitGeneticDataGenerator.CIRCUIT_STATE_KEY, circuitStateData);

    GeneticData outputMapData = new GeneticData(RandomUtil.randomBytes(
        TEST_CIRCUIT_OUTPUT_SIZE_BYTES, random), new int[0]);
    geneticDataStore.put(CircuitGeneticDataGenerator.OUTPUT_MAP_KEY, outputMapData);

    int[] bitOutputMap = new int[TEST_CIRCUIT_OUTPUT_SIZE];
    for (int idx = 0; idx < bitOutputMap.length; idx++) {
      bitOutputMap[idx] = random.nextInt(Integer.SIZE);
    }
    GeneticData bitOutputMapData = new GeneticData(ArrayUtil.toByteArray(bitOutputMap), new int[0]);
    geneticDataStore.put(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY, bitOutputMapData);
  }
}
