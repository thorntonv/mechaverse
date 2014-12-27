package org.mechaverse.simulation.common.cellautomaton.simulation;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonTestUtil.mockIntArrayAnswer;
import static org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonTestUtil.setRandomState;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonAllocator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CompositeCellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CompositeCellularAutomatonSimulator.CompositeCellularAutomatonSimulatorCloseException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for {@link CompositeCellularAutomatonSimulator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CompositeCellularAutomatonSimulatorTest {

  private static final int SIMULATOR1_AUTOMATON_COUNT = 3;
  private static final int SIMULATOR2_AUTOMATON_COUNT = 7;
  private static final int SIMULATOR3_AUTOMATON_COUNT = 5;

  @Mock CellularAutomatonSimulator mockSimulator1;
  @Mock CellularAutomatonSimulator mockSimulator2;
  @Mock CellularAutomatonSimulator mockSimulator3;

  private CompositeCellularAutomatonSimulator compositeSimulator;
  private CellularAutomatonSimulator[] componentSimulators;
  private int[] componentAutomatonIndexes;

  @Before
  public void setUp() {
    when(mockSimulator1.getAllocator())
        .thenReturn(new CellularAutomatonAllocator(SIMULATOR1_AUTOMATON_COUNT));
    when(mockSimulator2.getAllocator())
        .thenReturn(new CellularAutomatonAllocator(SIMULATOR2_AUTOMATON_COUNT));
    when(mockSimulator3.getAllocator())
        .thenReturn(new CellularAutomatonAllocator(SIMULATOR3_AUTOMATON_COUNT));

    compositeSimulator = new CompositeCellularAutomatonSimulator(
        ImmutableList.of(mockSimulator1, mockSimulator2, mockSimulator3));

    // Component simulators are interleaved.
    componentSimulators = new CellularAutomatonSimulator[] {
        mockSimulator1, mockSimulator2, mockSimulator3,
        mockSimulator1, mockSimulator2, mockSimulator3,
        mockSimulator1, mockSimulator2, mockSimulator3,
        mockSimulator2, mockSimulator3,
        mockSimulator2, mockSimulator3,
        mockSimulator2, mockSimulator2
    };
    componentAutomatonIndexes = new int[] {
       0, 0, 0,
       1, 1, 1,
       2, 2, 2,
       3, 3,
       4, 4,
       5, 6,
    };
  }

  @Test
  public void allocator() {
    CellularAutomatonAllocator allocator = compositeSimulator.getAllocator();
    assertEquals(
        SIMULATOR1_AUTOMATON_COUNT + SIMULATOR2_AUTOMATON_COUNT + SIMULATOR3_AUTOMATON_COUNT,
            allocator.getAvailableCount());
  }

  @Test
  public void size() {
    assertEquals(
        SIMULATOR1_AUTOMATON_COUNT + SIMULATOR2_AUTOMATON_COUNT + SIMULATOR3_AUTOMATON_COUNT,
            compositeSimulator.size());
  }

  @Test
  public void getAutomatonInputSize() {
    when(mockSimulator1.getAutomatonInputSize()).thenReturn(25);
    assertEquals(25, compositeSimulator.getAutomatonInputSize());
  }

  @Test
  public void getAutomatonStateSize() {
    when(mockSimulator1.getAutomatonStateSize()).thenReturn(55);
    assertEquals(55, compositeSimulator.getAutomatonStateSize());
  }

  @Test
  public void getAutomatonOutputSize() {
    when(mockSimulator1.getAutomatonOutputSize()).thenReturn(45);
    assertEquals(45, compositeSimulator.getAutomatonOutputSize());
  }

  @Test
  public void getAutomatonState() {
    for (int idx = 0; idx < compositeSimulator.size(); idx++) {
      int componentIdx = componentAutomatonIndexes[idx];
      CellularAutomatonSimulator mockSimulator = componentSimulators[idx];

      int[] expectedState = new int[100];
      setRandomState(expectedState);
      ArgumentCaptor<int[]> stateCaptor = ArgumentCaptor.forClass(int[].class);
      doAnswer(mockIntArrayAnswer(expectedState, stateCaptor)).when(mockSimulator)
          .getAutomatonState(eq(componentIdx), stateCaptor.capture());

      int[] state = new int[expectedState.length];
      compositeSimulator.getAutomatonState(idx, state);
      assertArrayEquals(expectedState, state);
    }
  }

  @Test
  public void setAutomatonState() {
    for (int idx = 0; idx < compositeSimulator.size(); idx++) {
      int componentIdx = componentAutomatonIndexes[idx];
      CellularAutomatonSimulator mockSimulator = componentSimulators[idx];

      int[] state = new int[100];
      setRandomState(state);

      compositeSimulator.setAutomatonState(idx, state);
      verify(mockSimulator).setAutomatonState(componentIdx, state);
    }
  }

  @Test
  public void setAutomatonInput() {
    for (int idx = 0; idx < compositeSimulator.size(); idx++) {
      int componentIdx = componentAutomatonIndexes[idx];
      CellularAutomatonSimulator mockSimulator = componentSimulators[idx];

      int[] input = new int[100];
      setRandomState(input);

      compositeSimulator.setAutomatonInput(idx, input);
      verify(mockSimulator).setAutomatonInput(componentIdx, input);
    }
  }

  @Test
  public void setAutomatonOutputMap() {
    for (int idx = 0; idx < compositeSimulator.size(); idx++) {
      int componentIdx = componentAutomatonIndexes[idx];
      CellularAutomatonSimulator mockSimulator = componentSimulators[idx];

      int[] outputMap = new int[100];
      setRandomState(outputMap);

      compositeSimulator.setAutomatonOutputMap(idx, outputMap);
      verify(mockSimulator).setAutomatonOutputMap(componentIdx, outputMap);
    }
  }

  @Test
  public void getAutomatonOutput() {
    for (int idx = 0; idx < compositeSimulator.size(); idx++) {
      int componentIdx = componentAutomatonIndexes[idx];
      CellularAutomatonSimulator mockSimulator = componentSimulators[idx];

      int[] expectedOutput = new int[8];
      setRandomState(expectedOutput);

      ArgumentCaptor<int[]> outputCaptor = ArgumentCaptor.forClass(int[].class);
      doAnswer(mockIntArrayAnswer(expectedOutput, outputCaptor)).when(mockSimulator)
          .getAutomatonOutput(eq(componentIdx), outputCaptor.capture());

      int[] output = new int[expectedOutput.length];
      compositeSimulator.getAutomatonOutput(idx, output);
      assertArrayEquals(expectedOutput, output);
    }
  }

  @Test
  public void update() {
    compositeSimulator.update();

    verify(mockSimulator1).update();
    verify(mockSimulator2).update();
    verify(mockSimulator3).update();
  }

  @Test
  public void close() throws Exception {
    compositeSimulator.close();

    verify(mockSimulator1).close();
    verify(mockSimulator2).close();
    verify(mockSimulator3).close();
  }

  @Test
  public void closeError() throws Exception {
    doThrow(new IOException()).when(mockSimulator2).close();

    try {
      compositeSimulator.close();
      fail("Expected exception was not thrown");
    } catch (CompositeCellularAutomatonSimulatorCloseException ex) {
      // Expected.
      assertEquals(1, ex.getExceptions().size());
    }

    verify(mockSimulator1).close();
    verify(mockSimulator3).close();
  }
}
