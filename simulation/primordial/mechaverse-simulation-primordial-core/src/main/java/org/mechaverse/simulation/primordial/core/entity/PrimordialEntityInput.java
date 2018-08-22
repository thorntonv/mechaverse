package org.mechaverse.simulation.primordial.core.entity;

import com.google.common.math.IntMath;
import java.math.RoundingMode;
import org.mechaverse.simulation.primordial.core.model.EntityType;

/**
 * Encodes and decodes input for a primordial entity.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class PrimordialEntityInput {

  /**
   * Sensor information about an entity in a cell.
   */
  public static final class SensorInfo {

    private EntityType entityType;

    public SensorInfo(EntityType entityType) {
      super();
      this.entityType = entityType;
    }

    public EntityType getEntityType() {
      return entityType;
    }
  }


  public static int DATA_SIZE_BITS = 6;

  private static final int ENERGY_LEVEL_IDX = 0;

  private static final int FRONT_SENSOR_IDX = 2;

  private static final int ENTITY_SENSOR_IDX = 4;

  private static final int FOOD_SENSOR_IDX = 5;

  private final int[] data;

  public PrimordialEntityInput() {
    this(new int[IntMath.divide(DATA_SIZE_BITS, Integer.SIZE, RoundingMode.CEILING)]);
  }

  public PrimordialEntityInput(int[] data) {
    this.data = data;
  }

  public int getEnergyLevel() {
    return getBits(0, ENERGY_LEVEL_IDX, 2);
  }

  public final void setInput(int energyLevel, int maxEnergyLevel, int frontEntityOrdinal,
      boolean nearbyEntity, boolean nearbyFood) {
    int value = energyLevel * 3 / maxEnergyLevel;
    value <<= 2;
    value |= frontEntityOrdinal & 0b11;
    value <<= 1;
    value |= nearbyEntity ? 1 : 0;
    value <<= 1;
    value |= nearbyFood ? 1 : 0;

    data[0] = value;
  }

  /**
   * Sets the energy level for the entity. It is represented using 2 bits (0 = low, 3 = high).
   */
  public void setEnergy(int energyLevel, int maxEnergyLevel) {
    // Use a direct proportion to map the energy level on to the new range.
    int energyPercent = energyLevel * 3 / maxEnergyLevel;
    setBits(0, ENERGY_LEVEL_IDX, 2, energyPercent);
  }

  public int getFrontEntityTypeOrdinal() {
    return getBits(0, FRONT_SENSOR_IDX, 2);
  }

  public void setFrontEntityType(int entityTypeOrdinal) {
    setBits(0, FRONT_SENSOR_IDX, 2, entityTypeOrdinal);
  }

  public boolean getEntitySensor() {
    return getBits(0, ENTITY_SENSOR_IDX, 1) == 1;
  }

  public void setEntitySensor(boolean nearbyEntity) {
    setBits(0, ENTITY_SENSOR_IDX, 1, nearbyEntity ? 1 : 0);
  }

  public boolean getFoodSensor() {
    return getBits(0, FOOD_SENSOR_IDX, 1) == 1;
  }

  public void setFoodSensor(boolean nearbyFood) {
    setBits(0, FOOD_SENSOR_IDX, 1, nearbyFood ? 1 : 0);
  }

  public int[] getData() {
    return data;
  }

  public void resetToDefault() {
    data[0] = 0;
  }

  private int getBits(int idx, int bitOffset, int bitCount) {
    int mask = (1 << bitCount) - 1;
    return data[idx] >>> bitOffset & mask;
  }

  private void setBits(int idx, int bitOffset, int bitCount, int value) {
    int srcMask = (1 << bitCount) - 1;
    int destMask = ~(srcMask << bitOffset);
    data[idx] = (data[idx] & destMask) | ((value & srcMask) << bitOffset);
  }
}
