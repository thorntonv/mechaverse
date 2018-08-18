package org.mechaverse.simulation.primordial.core.entity;

import org.junit.Test;
import org.mechaverse.simulation.primordial.core.model.EntityType;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PrimordialEntityInputTest {

    @Test
    public void testGetEnergy() {
        int[] expectedValues = new int[]{3, 2, 1, 0};
        int[][] inputData = new int[][]{
                {1, 1, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}};

        for (int idx = 0; idx < expectedValues.length; idx++) {
            PrimordialEntityInput input = new PrimordialEntityInput(inputData[idx]);
            assertEquals(expectedValues[idx], input.getEnergyLevel());
            assertEquals(EntityType.NONE, input.getFrontSensor().getEntityType());
            assertFalse(input.getEntitySensor());
            assertFalse(input.getFoodSensor());
        }
    }

    @Test
    public void testSetEnergy() {
        int[] inputData = {100, 70, 45, 10};
        int[][] expectedValues = new int[][]{
                {1, 1, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        for (int idx = 0; idx < inputData.length; idx++) {
            PrimordialEntityInput input = new PrimordialEntityInput();
            input.setEnergy(inputData[idx], 100);
            assertArrayEquals("idx=" + idx, expectedValues[idx], input.getData());
        }
    }
}