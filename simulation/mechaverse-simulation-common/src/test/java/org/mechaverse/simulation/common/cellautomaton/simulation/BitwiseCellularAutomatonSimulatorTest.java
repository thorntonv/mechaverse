package org.mechaverse.simulation.common.cellautomaton.simulation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BitwiseCellularAutomatonSimulatorTest {

  @Mock
  private CellularAutomatonSimulator mockSimulator;

  @Before
  public void setUp() {
    resetMockSimulator(3);
  }

  @Test
  public void testAllocate() {
    BitwiseCellularAutomatonSimulator bitwiseSimulator = new BitwiseCellularAutomatonSimulator(
        mockSimulator, 4);
    assertEquals(64, bitwiseSimulator.getAllocator().getAvailableCount());
    for (int cnt = 1; cnt <= 64; cnt++) {
      assertEquals(cnt - 1, bitwiseSimulator.getAllocator().allocate());
    }
    try {
      bitwiseSimulator.getAllocator().allocate();
      fail("Expected exception was not thrown");
    } catch (IllegalStateException ex) {
      // Expected.
    }
  }

  @Test
  public void testSetAutomatonState_bitwiseIndex1_bitsPerEntity4() {
    testSetAutomatonState(
        new int[]{0b010101010100, 0b010101110101, 0b010110110101},
        1,
        new int[]{0b101_01111011},
        new int[]{0b010101010100, 0b010101010101, 0b010101010101},
        4);
  }

  @Test
  public void testGetAutomatonState_bitwiseIndex1_bitsPerEntity4() {
    testGetAutomatonState(
        new int[]{0b101_01111011},
        1,
        new int[]{0b010101010100, 0b010101110101, 0b010110110101},
        4);
  }

  @Test
  public void testSetAutomatonState_bitwiseIndex5_bitsPerEntity1() {
    testSetAutomatonState(
        new int[]{0b0101_01110100, 0b0101_01010101, 0b0101_10010101},
        5,
        new int[]{0b100},
        new int[]{0b0101_01010100, 0b0101_01110101, 0b0101_10110101},

        1);
  }

  @Test
  public void testGetAutomatonState_bitwiseIndex5_bitsPerEntity1() {
    testGetAutomatonState(
        new int[]{0b100},
        5,
        new int[]{0b0101_01110100, 0b0101_01010101, 0b0101_10010101},
        1);
  }

  @Test
  public void testSetAutomatonState_bitwiseIndex17_bitsPerEntity4() {
    testSetAutomatonState(
        new int[]{0b010111010100, 0b010111100101, 0b010110110101},
        17,
        new int[]{0b1101_11101011},
        new int[]{0b010101010100, 0b010101010101, 0b010101010101},
        4);
  }

  @Test
  public void testGetAutomatonState_bitwiseIndex17_bitsPerEntity4() {
    testGetAutomatonState(
        new int[]{0b1101_11101011},
        17,
        new int[]{0b010111010100, 0b010111100101, 0b010110110101},
        4);
  }

  @Test
  public void testSetAutomatonState_bitwiseIndex17_bitsPerEntity4_size48() {
    int[] expected = new int[12];
    int[] inputData = new int[12];
    for (int idx = 0; idx < expected.length; idx++) {
      expected[idx] = 0b10100000;
      inputData[idx] = 0;
    }
    testSetAutomatonState(
        expected,
        17,
        new int[]{0b10101010101010101010101010101010, 0b1010101010101010},
        inputData,
        4);
  }

  @Test
  public void testGetAutomatonState_bitwiseIndex17_bitsPerEntity4_size48() {
    int[] existingData = new int[12];
    for (int idx = 0; idx < existingData.length; idx++) {
      existingData[idx] = 0b10100000;
    }
    testGetAutomatonState(
        new int[]{0b10101010101010101010101010101010, 0b1010101010101010},
        17,
        existingData,
        4);
  }

  private void testSetAutomatonState(int[] expectedState, int bitwiseAutomatonIndex,
      int[] stateToSet,
      int[] existingState, int bitsPerEntity) {
    resetMockSimulator(expectedState.length);
    int mockAutomatonIndex = bitwiseAutomatonIndex * bitsPerEntity / Integer.SIZE;
    ArgumentCaptor<int[]> argumentCaptor = ArgumentCaptor.forClass(int[].class);
    doAnswer(copyArrayAnswer(existingState, argumentCaptor))
        .when(mockSimulator).getAutomatonState(eq(mockAutomatonIndex), argumentCaptor.capture());
    BitwiseCellularAutomatonSimulator bitwiseSimulator = new BitwiseCellularAutomatonSimulator(
        mockSimulator, bitsPerEntity);
    bitwiseSimulator.invalidateCaches();
    bitwiseSimulator.setAutomatonState(bitwiseAutomatonIndex, stateToSet);
    bitwiseSimulator.update();

    argumentCaptor = ArgumentCaptor.forClass(int[].class);
    verify(mockSimulator).setAutomatonState(eq(mockAutomatonIndex), argumentCaptor.capture());
    assertArrayEquals(expectedState, argumentCaptor.getValue());
  }

  private void testGetAutomatonState(int[] expectedState, int bitwiseAutomatonIndex,
      int[] existingState, int bitsPerEntity) {
    resetMockSimulator(existingState.length);
    int[] stateToGet = new int[expectedState.length];
    int mockAutomatonIndex = bitwiseAutomatonIndex * bitsPerEntity / Integer.SIZE;
    ArgumentCaptor<int[]> argumentCaptor = ArgumentCaptor.forClass(int[].class);
    doAnswer(copyArrayAnswer(existingState, argumentCaptor))
        .when(mockSimulator).getAutomatonState(eq(mockAutomatonIndex), argumentCaptor.capture());
    BitwiseCellularAutomatonSimulator bitwiseSimulator = new BitwiseCellularAutomatonSimulator(
        mockSimulator, bitsPerEntity);
    bitwiseSimulator.invalidateCaches();
    bitwiseSimulator.getAutomatonState(bitwiseAutomatonIndex, stateToGet);

    assertArrayEquals(expectedState, stateToGet);
  }


  private Answer copyArrayAnswer(int[] value, ArgumentCaptor<int[]> argumentCaptor) {
    return invocation -> {
      System.arraycopy(value, 0, argumentCaptor.getValue(), 0, value.length);
      return null;
    };
  }

  private void resetMockSimulator(int stateSize) {
    reset(mockSimulator);
    when(mockSimulator.size()).thenReturn(8);
    when(mockSimulator.getAutomatonStateSize()).thenReturn(stateSize);
  }
}