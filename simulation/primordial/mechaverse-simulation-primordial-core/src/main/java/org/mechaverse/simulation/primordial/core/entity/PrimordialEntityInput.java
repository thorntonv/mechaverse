package org.mechaverse.simulation.primordial.core.entity;

import org.mechaverse.simulation.common.model.Direction;
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


  public static int DATA_SIZE = 6;

  private static final int ENERGY_LEVEL_IDX = 0;

  private static final int FRONT_SENSOR_IDX = 2;

  private static final int ENTITY_SENSOR_IDX = 4;

  private static final int FOOD_SENSOR_IDX = 5;

  private final int[] data;

  public PrimordialEntityInput() {
    this(new int[DATA_SIZE]);
  }

  public PrimordialEntityInput(int[] data) {
    this.data = data;
  }

  public int getEnergyLevel() {
    return getBits(ENERGY_LEVEL_IDX, 2);
  }

  /**
   * Sets the energy level for the entity. It is represented using 2 bits (0 = low, 3 = high).
   */
  public void setEnergy(int energyLevel, int maxEnergyLevel) {
    // Use a direct proportion to map the energy level on to the new range.
    int energyPercent = energyLevel * 100 / maxEnergyLevel;
    int value = 0;
    if (energyPercent >= 75) {
      value = 3;
    } else if (energyPercent >= 50) {
      value = 2;
    } else if (energyPercent >= 33) {
      value = 1;
    }
    setBits(ENERGY_LEVEL_IDX, 2, value);
  }

  public SensorInfo getFrontSensor() {
    return getSensorData(FRONT_SENSOR_IDX);
  }

  public void setFrontSensor(EntityType entityType, Direction entityDirection, String entityId) {
    setSensorData(entityType, FRONT_SENSOR_IDX);
  }

  public boolean getEntitySensor() {
    return getBits(ENTITY_SENSOR_IDX, 1) == 1;
  }

  public void setEntitySensor(boolean nearbyEntity) {
    setBits(ENTITY_SENSOR_IDX, 1, nearbyEntity ? 1 : 0);
  }

  public boolean getFoodSensor() {
    return getBits(FOOD_SENSOR_IDX, 1) == 1;
  }

  public void setFoodSensor(boolean nearbyFood) {
    setBits(FOOD_SENSOR_IDX, 1, nearbyFood ? 1 : 0);
  }

  public int[] getData() {
    return data;
  }

  public void resetToDefault() {
    for (int idx = 0; idx < data.length; idx++) {
      data[idx] = 0;
    }
  }

  private SensorInfo getSensorData(int idx) {
    EntityType entityType = getEntityType(idx);
    return new SensorInfo(entityType);
  }

  private void setSensorData(EntityType entityType, int idx) {
    setEntityType(entityType, idx);
  }

  private EntityType getEntityType(int idx) {
    int entityTypeValue = getBits(idx, 2);
    return entityTypeValue < EntityUtil.ENTITY_TYPES.length ? EntityUtil.ENTITY_TYPES[entityTypeValue] : null;
  }

  private void setEntityType(EntityType entityType, int idx) {
    int entityTypeValue = entityType != null ? entityType.ordinal() : 0b11;
    setBits(idx, 2, entityTypeValue);
  }

  private int getBits(int idx, int bitCount) {
    int value = 0;
    for (int cnt = 1; cnt <= bitCount; cnt++) {
      value = (value << 1) | (data[idx] & 0b1);
      idx++;
    }
    return value;
  }

  private void setBits(int idx, int bitCount, int value) {
    idx += bitCount - 1;
    for (int cnt = 1; cnt <= bitCount; cnt++) {
      data[idx] = data[idx] & ~0b1 | (value & 0b1);
      idx--;
      value >>= 1;
    }
  }
}
