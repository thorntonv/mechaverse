package org.mechaverse.simulation.ant.core.entity.ant;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.mechaverse.simulation.common.circuit.CircuitAllocator;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.mechaverse.simulation.common.circuit.CircuitUtil;
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
  @Mock private SimulationDataStore mockState;

  private CircuitAntBehavior behavior;
  private AntInput input;
  private RandomGenerator random;

  @Before
  public void setUp() {
    when(mockCircuitSimulator.getAllocator()).thenReturn(mockCircuitAllocator);
    when(mockCircuitAllocator.allocateCircuit()).thenReturn(0);
    when(mockCircuitSimulator.getCircuitStateSize()).thenReturn(TEST_CIRCUIT_STATE_SIZE);
    when(mockCircuitSimulator.getCircuitOutputSize()).thenReturn(TEST_CIRCUIT_OUTPUT_SIZE);
    input = new AntInput();
    random = RandomUtil.newGenerator(CircuitAntBehavior.class.getName().hashCode());
    behavior = new CircuitAntBehavior(mockCircuitSimulator);
  }

  @Test
  public void generateGeneticData() throws IOException {
    behavior.setState(mockState);
    behavior.setInput(input, random);

    // Verify that the circuit state was initialized.

    ArgumentCaptor<byte[]> circuitStateCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(mockState).put(eq(CircuitAntBehavior.CIRCUIT_STATE_KEY), circuitStateCaptor.capture());
    assertEquals(TEST_CIRCUIT_STATE_SIZE_BYTES, circuitStateCaptor.getValue().length);

    ArgumentCaptor<byte[]> outputMapCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(mockState).put(eq(CircuitAntBehavior.CIRCUIT_OUTPUT_MAP_KEY), outputMapCaptor.capture());
    assertEquals(TEST_CIRCUIT_OUTPUT_SIZE_BYTES, outputMapCaptor.getValue().length);

    ArgumentCaptor<byte[]> bitOutputMapCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(mockState).put(eq(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY),
        bitOutputMapCaptor.capture());
    assertEquals(TEST_CIRCUIT_OUTPUT_SIZE_BYTES, bitOutputMapCaptor.getValue().length);

    // Verify that the genetic data was stored.
    ArgumentCaptor<byte[]> geneticDataCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(mockState).put(eq(GeneticDataStore.KEY), geneticDataCaptor.capture());
    GeneticDataStore geneticData = GeneticDataStore.deserialize(geneticDataCaptor.getValue());
    assertEquals(ImmutableSet.of(CircuitGeneticDataGenerator.CIRCUIT_STATE_KEY,
        CircuitGeneticDataGenerator.OUTPUT_MAP_KEY, CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY),
            geneticData.keySet());

    // Verify that the stored genetic data matches the initial circuit state.

    GeneticData circuitStateData = geneticData.get(CircuitGeneticDataGenerator.CIRCUIT_STATE_KEY);
    assertArrayEquals(circuitStateCaptor.getValue(), circuitStateData.getData());
    assertNotNull(circuitStateData.getCrossoverPoints());

    GeneticData outputMapData = geneticData.get(CircuitGeneticDataGenerator.OUTPUT_MAP_KEY);
    assertArrayEquals(outputMapCaptor.getValue(), outputMapData.getData());
    assertNotNull(outputMapData.getCrossoverPoints());

    GeneticData bitOutputMapData = geneticData.get(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY);
    assertArrayEquals(bitOutputMapCaptor.getValue(), bitOutputMapData.getData());
    assertNotNull(bitOutputMapData.getCrossoverPoints());
  }

  @Test
  public void loadGeneticData() throws IOException {
    GeneticDataStore geneticData = createGeneticDataStore();
    when(mockState.containsKey(GeneticDataStore.KEY)).thenReturn(true);
    when(mockState.get(GeneticDataStore.KEY)).thenReturn(geneticData.serialize());
    behavior.setState(mockState);
    behavior.setInput(input, random);

    // Verify that the initial state was stored.

    ArgumentCaptor<byte[]> circuitStateCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(mockState).put(eq(CircuitAntBehavior.CIRCUIT_STATE_KEY), circuitStateCaptor.capture());
    assertArrayEquals(geneticData.get(CircuitGeneticDataGenerator.CIRCUIT_STATE_KEY).getData(),
        circuitStateCaptor.getValue());

    ArgumentCaptor<byte[]> outputMapCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(mockState).put(eq(CircuitAntBehavior.CIRCUIT_OUTPUT_MAP_KEY), outputMapCaptor.capture());
    assertArrayEquals(geneticData.get(CircuitGeneticDataGenerator.OUTPUT_MAP_KEY).getData(),
        outputMapCaptor.getValue());

    ArgumentCaptor<byte[]> bitOutputMapCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(mockState).put(eq(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY),
        bitOutputMapCaptor.capture());
    assertArrayEquals(geneticData.get(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY).getData(),
      bitOutputMapCaptor.getValue());

    // Verify that the circuit simulator was initialized.

    verify(mockCircuitSimulator).setCircuitState(eq(0),
        eq(ArrayUtil.toIntArray(circuitStateCaptor.getValue())));
    verify(mockCircuitSimulator).setCircuitOutputMap(eq(0),
        eq(ArrayUtil.toIntArray(outputMapCaptor.getValue())));
  }

  @Test
  public void setInput() {
    input = new AntInput(
        ArrayUtil.toIntArray(RandomUtil.randomBytes(AntInput.DATA_SIZE * 4, random)));
    behavior.setInput(input, random);
    verify(mockCircuitSimulator).setCircuitInput(0, input.getData());
  }

  @Test
  public void getOutput() throws IOException {
    GeneticDataStore geneticData = createGeneticDataStore();
    when(mockState.containsKey(GeneticDataStore.KEY)).thenReturn(true);
    when(mockState.get(GeneticDataStore.KEY)).thenReturn(geneticData.serialize());

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
    behavior.setState(mockState);
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
    behavior.onRemoveEntity();

    verify(mockCircuitAllocator).deallocateCircuit(0);
  }

  @Test
  public void setState() {
    byte[] circuitStateBytes = RandomUtil.randomBytes(TEST_CIRCUIT_STATE_SIZE_BYTES, random);
    byte[] outputMapBytes = RandomUtil.randomBytes(TEST_CIRCUIT_OUTPUT_SIZE_BYTES, random);
    when(mockState.get(CircuitAntBehavior.CIRCUIT_STATE_KEY)).thenReturn(circuitStateBytes);
    when(mockState.get(CircuitAntBehavior.CIRCUIT_OUTPUT_MAP_KEY)).thenReturn(outputMapBytes);

    behavior.setState(mockState);

    verify(mockCircuitSimulator)
        .setCircuitState(eq(0), eq(ArrayUtil.toIntArray(circuitStateBytes)));
    verify(mockCircuitSimulator)
        .setCircuitOutputMap(eq(0), eq(ArrayUtil.toIntArray(outputMapBytes)));
  }

  @Test
  public void getState() {
    final byte[] circuitStateBytes = RandomUtil.randomBytes(TEST_CIRCUIT_STATE_SIZE_BYTES, random);
    when(mockState.get(CircuitAntBehavior.CIRCUIT_STATE_KEY)).thenReturn(circuitStateBytes);
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

    behavior.setState(mockState);
    SimulationDataStore state = behavior.getState();
    assertEquals(mockState, state);
    verify(mockState).put(eq(CircuitAntBehavior.CIRCUIT_STATE_KEY), eq(circuitStateBytes));
  }

  private GeneticDataStore createGeneticDataStore() {
    GeneticDataStore dataStore = new GeneticDataStore();

    GeneticData circuitStateData = new GeneticData(RandomUtil.randomBytes(
        TEST_CIRCUIT_STATE_SIZE_BYTES, random), new int[0]);
    dataStore.put(CircuitGeneticDataGenerator.CIRCUIT_STATE_KEY, circuitStateData);

    GeneticData outputMapData = new GeneticData(RandomUtil.randomBytes(
        TEST_CIRCUIT_OUTPUT_SIZE_BYTES, random), new int[0]);
    dataStore.put(CircuitGeneticDataGenerator.OUTPUT_MAP_KEY, outputMapData);

    int[] bitOutputMap = new int[TEST_CIRCUIT_OUTPUT_SIZE];
    for (int idx = 0; idx < bitOutputMap.length; idx++) {
      bitOutputMap[idx] = random.nextInt(Integer.SIZE);
    }
    GeneticData bitOutputMapData = new GeneticData(ArrayUtil.toByteArray(bitOutputMap), new int[0]);
    dataStore.put(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY, bitOutputMapData);

    return dataStore;
  }
}
