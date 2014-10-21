package org.mechaverse.simulation.common.circuit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mechaverse.simulation.common.circuit.CircuitTestUtil.mockIntArrayAnswer;
import static org.mechaverse.simulation.common.circuit.CircuitTestUtil.setRandomState;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.common.circuit.CompositeCircuitSimulator.CompositeCircuitSimulatorCloseException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for {@link CompositeCircuitSimulator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CompositeCircuitSimulatorTest {

  private static final int SIMULATOR1_CIRCUIT_COUNT = 3;
  private static final int SIMULATOR2_CIRCUIT_COUNT = 7;
  private static final int SIMULATOR3_CIRCUIT_COUNT = 5;

  @Mock CircuitSimulator mockSimulator1;
  @Mock CircuitSimulator mockSimulator2;
  @Mock CircuitSimulator mockSimulator3;

  private CompositeCircuitSimulator compositeSimulator;
  private CircuitSimulator[] componentSimulators;
  private int[] componentCircuitIndexes;

  @Before
  public void setUp() {
    when(mockSimulator1.getAllocator()).thenReturn(new CircuitAllocator(SIMULATOR1_CIRCUIT_COUNT));
    when(mockSimulator2.getAllocator()).thenReturn(new CircuitAllocator(SIMULATOR2_CIRCUIT_COUNT));
    when(mockSimulator3.getAllocator()).thenReturn(new CircuitAllocator(SIMULATOR3_CIRCUIT_COUNT));

    compositeSimulator = new CompositeCircuitSimulator(
        ImmutableList.of(mockSimulator1, mockSimulator2, mockSimulator3));

    // Component simulators are interleaved.
    componentSimulators = new CircuitSimulator[] {
        mockSimulator1, mockSimulator2, mockSimulator3,
        mockSimulator1, mockSimulator2, mockSimulator3,
        mockSimulator1, mockSimulator2, mockSimulator3,
        mockSimulator2, mockSimulator3,
        mockSimulator2, mockSimulator3,
        mockSimulator2, mockSimulator2
    };
    componentCircuitIndexes = new int[] {
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
    CircuitAllocator allocator = compositeSimulator.getAllocator();
    assertEquals(SIMULATOR1_CIRCUIT_COUNT + SIMULATOR2_CIRCUIT_COUNT + SIMULATOR3_CIRCUIT_COUNT,
        allocator.getAvailableCircuitCount());
  }

  @Test
  public void circuitCount() {
    assertEquals(SIMULATOR1_CIRCUIT_COUNT + SIMULATOR2_CIRCUIT_COUNT + SIMULATOR3_CIRCUIT_COUNT,
        compositeSimulator.getCircuitCount());
  }

  @Test
  public void getCircuitInputSize() {
    when(mockSimulator1.getCircuitInputSize()).thenReturn(25);
    assertEquals(25, compositeSimulator.getCircuitInputSize());
  }

  @Test
  public void getCircuitStateSize() {
    when(mockSimulator1.getCircuitStateSize()).thenReturn(55);
    assertEquals(55, compositeSimulator.getCircuitStateSize());
  }

  @Test
  public void getCircuitOutputSize() {
    when(mockSimulator1.getCircuitOutputSize()).thenReturn(45);
    assertEquals(45, compositeSimulator.getCircuitOutputSize());
  }

  @Test
  public void getCircuitState() {
    for (int circuitIdx = 0; circuitIdx < compositeSimulator.getCircuitCount(); circuitIdx++) {
      int componentCircuitIdx = componentCircuitIndexes[circuitIdx];
      CircuitSimulator mockSimulator = componentSimulators[circuitIdx];

      int[] expectedCircuitState = new int[100];
      setRandomState(expectedCircuitState);
      ArgumentCaptor<int[]> circuitStateCaptor = ArgumentCaptor.forClass(int[].class);
      doAnswer(mockIntArrayAnswer(expectedCircuitState, circuitStateCaptor)).when(mockSimulator)
          .getCircuitState(eq(componentCircuitIdx), circuitStateCaptor.capture());

      int[] circuitState = new int[expectedCircuitState.length];
      compositeSimulator.getCircuitState(circuitIdx, circuitState);
      assertArrayEquals(expectedCircuitState, circuitState);
    }
  }

  @Test
  public void setCircuitState() {
    for (int circuitIdx = 0; circuitIdx < compositeSimulator.getCircuitCount(); circuitIdx++) {
      int componentCircuitIdx = componentCircuitIndexes[circuitIdx];
      CircuitSimulator mockSimulator = componentSimulators[circuitIdx];

      int[] circuitState = new int[100];
      setRandomState(circuitState);

      compositeSimulator.setCircuitState(circuitIdx, circuitState);
      verify(mockSimulator).setCircuitState(componentCircuitIdx, circuitState);
    }
  }

  @Test
  public void setCircuitInput() {
    for (int circuitIdx = 0; circuitIdx < compositeSimulator.getCircuitCount(); circuitIdx++) {
      int componentCircuitIdx = componentCircuitIndexes[circuitIdx];
      CircuitSimulator mockSimulator = componentSimulators[circuitIdx];

      int[] circuitInput = new int[100];
      setRandomState(circuitInput);

      compositeSimulator.setCircuitInput(circuitIdx, circuitInput);
      verify(mockSimulator).setCircuitInput(componentCircuitIdx, circuitInput);
    }
  }

  @Test
  public void setCircuitOutputMap() {
    for (int circuitIdx = 0; circuitIdx < compositeSimulator.getCircuitCount(); circuitIdx++) {
      int componentCircuitIdx = componentCircuitIndexes[circuitIdx];
      CircuitSimulator mockSimulator = componentSimulators[circuitIdx];

      int[] circuitOutputMap = new int[100];
      setRandomState(circuitOutputMap);

      compositeSimulator.setCircuitOutputMap(circuitIdx, circuitOutputMap);
      verify(mockSimulator).setCircuitOutputMap(componentCircuitIdx, circuitOutputMap);
    }
  }

  @Test
  public void getCircuitOutput() {
    for (int circuitIdx = 0; circuitIdx < compositeSimulator.getCircuitCount(); circuitIdx++) {
      int componentCircuitIdx = componentCircuitIndexes[circuitIdx];
      CircuitSimulator mockSimulator = componentSimulators[circuitIdx];

      int[] expectedCircuitOutput = new int[8];
      setRandomState(expectedCircuitOutput);

      ArgumentCaptor<int[]> circuitOutputCaptor = ArgumentCaptor.forClass(int[].class);
      doAnswer(mockIntArrayAnswer(expectedCircuitOutput, circuitOutputCaptor)).when(mockSimulator)
          .getCircuitOutput(eq(componentCircuitIdx), circuitOutputCaptor.capture());

      int[] circuitOutput = new int[expectedCircuitOutput.length];
      compositeSimulator.getCircuitOutput(circuitIdx, circuitOutput);
      assertArrayEquals(expectedCircuitOutput, circuitOutput);
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
    } catch (CompositeCircuitSimulatorCloseException ex) {
      // Expected.
      assertEquals(1, ex.getExceptions().size());
    }

    verify(mockSimulator1).close();
    verify(mockSimulator3).close();
  }
}
