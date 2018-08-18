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
        resetMockSimulator();
    }

    @Test
    public void testAllocate() {
        BitwiseCellularAutomatonSimulator bitwiseSimulator = new BitwiseCellularAutomatonSimulator(mockSimulator, 4);
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
                new int[]{0b101, 0b111, 0b1011},
                new int[]{0b010101010100, 0b010101010101, 0b010101010101},
                4);
    }

    @Test
    public void testSetAutomatonState_bitwiseIndex5_bitsPerEntity1() {
        testSetAutomatonState(
                new int[]{0b0101_01110100, 0b0101_01010101, 0b0101_10010101},
                5,
                new int[]{0b1, 0b0, 0b0},
                new int[]{0b0101_01010100, 0b0101_01110101, 0b0101_10110101},

                1);
    }

    @Test
    public void testSetAutomatonState_bitwiseIndex17_bitsPerEntity4() {
        testSetAutomatonState(
                new int[]{0b010111010100, 0b010111100101, 0b010110110101},
                17,
                new int[]{0b1101, 0b1110, 0b1011},
                new int[]{0b010101010100, 0b010101010101, 0b010101010101},
                4);
    }

    private void testSetAutomatonState(int[] expectedState, int bitwiseAutomatonIndex, int[] stateToSet,
                                       int[] existingState, int bitsPerEntity) {
        resetMockSimulator();
        int mockAutomatonIndex = bitwiseAutomatonIndex * bitsPerEntity / Integer.SIZE;
        ArgumentCaptor<int[]> argumentCaptor = ArgumentCaptor.forClass(int[].class);
        doAnswer(copyArrayAnswer(existingState, argumentCaptor))
                .when(mockSimulator).getAutomatonState(eq(mockAutomatonIndex), argumentCaptor.capture());
        BitwiseCellularAutomatonSimulator bitwiseSimulator = new BitwiseCellularAutomatonSimulator(mockSimulator, bitsPerEntity);
        bitwiseSimulator.setAutomatonState(bitwiseAutomatonIndex, stateToSet);
        bitwiseSimulator.update();

        argumentCaptor = ArgumentCaptor.forClass(int[].class);
        verify(mockSimulator).setAutomatonState(eq(mockAutomatonIndex), argumentCaptor.capture());
        assertArrayEquals(expectedState, argumentCaptor.getValue());
    }

    private Answer copyArrayAnswer(int[] value, ArgumentCaptor<int[]> argumentCaptor) {
        return invocation -> {
            System.arraycopy(value, 0, argumentCaptor.getValue(), 0, value.length);
            return null;
        };
    }

    private void resetMockSimulator() {
        reset(mockSimulator);
        when(mockSimulator.size()).thenReturn(8);
        when(mockSimulator.getAutomatonStateSize()).thenReturn(3);
    }
}