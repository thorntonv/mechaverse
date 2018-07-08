package org.mechaverse.simulation.primordial.core.entity.primordial;

import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;
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


  public static int DATA_SIZE = 4;

  private static final int ENERGY_LEVEL_IDX = 0;
  private static final int ENERGY_LEVEL_MASK = ~0b11;

  private static final int FRONT_SENSOR_IDX = 1;
  private static final int FRONT_ENTITY_TYPE_BIT_IDX = 0;
  private static final int FRONT_ENTITY_TYPE_MASK = ~(0b11 << FRONT_ENTITY_TYPE_BIT_IDX);

  private static final int ENTITY_SENSOR_IDX = 2;
  private static final int ENTITY_SENSOR_BIT_IDX = 0;
  private static final int ENTITY_SENSOR_MASK = ~(0b1 << ENTITY_SENSOR_BIT_IDX);

  private static final int FOOD_SENSOR_IDX = 3;
  private static final int FOOD_SENSOR_BIT_IDX = 0;
  private static final int FOOD_SENSOR_MASK = ~(0b1 << FOOD_SENSOR_BIT_IDX);

  private final int[] data;

  public PrimordialEntityInput() {
    this(new int[DATA_SIZE]);
  }

  public PrimordialEntityInput(int[] data) {
    this.data = data;
  }

  public int getEnergyLevel() {
    return data[ENERGY_LEVEL_IDX] & ~ENERGY_LEVEL_MASK;
  }

  /**
   * Sets the energy level for the entity. It is represented using 2 bits (0 = low, 3 = high).
   */
  public void setEnergy(int energyLevel, int maxEnergyLevel) {
    // Use a direct proportion to map the energy level on to the new range.
    int value = 0;
    int energyPercent = energyLevel * 100 / maxEnergyLevel;
    value = (energyPercent < 33) ? 1 : 0;
    data[ENERGY_LEVEL_IDX] = (data[ENERGY_LEVEL_IDX] & ENERGY_LEVEL_MASK) | value;
  }

  public SensorInfo getFrontSensor() {
    return getSensorData(FRONT_SENSOR_IDX, FRONT_ENTITY_TYPE_MASK, FRONT_ENTITY_TYPE_BIT_IDX);
  }

  public void setFrontSensor(EntityType entityType, Direction entityDirection, String entityId) {
    setSensorData(entityType, FRONT_SENSOR_IDX, FRONT_ENTITY_TYPE_MASK, FRONT_ENTITY_TYPE_BIT_IDX);
  }

  public boolean getEntitySensor() {
    int dataValue = data[ENTITY_SENSOR_IDX];
    int nearbyEntity = ((dataValue & ~ENTITY_SENSOR_MASK) >> ENTITY_SENSOR_BIT_IDX) & 0b1;
    return nearbyEntity == 1;
  }

  public void setEntitySensor(boolean nearbyEntity) {
    int nearbyEntityIntValue = nearbyEntity ? 1 : 0;
    int dataValue = data[ENTITY_SENSOR_IDX];
    dataValue = (dataValue & ENTITY_SENSOR_MASK) | (nearbyEntityIntValue << ENTITY_SENSOR_BIT_IDX);
    data[ENTITY_SENSOR_IDX] = dataValue;
  }

  public boolean getFoodSensor() {
    int dataValue = data[FOOD_SENSOR_IDX];
    int nearbyFood = ((dataValue & ~FOOD_SENSOR_MASK) >> FOOD_SENSOR_BIT_IDX) & 0b1;
    return nearbyFood == 1;
  }

  public void setFoodSensor(boolean nearbyFood) {
    int nearbyFoodIntValue = nearbyFood ? 1 : 0;
    int dataValue = data[FOOD_SENSOR_IDX];
    dataValue = (dataValue & FOOD_SENSOR_MASK) | (nearbyFoodIntValue << FOOD_SENSOR_BIT_IDX);
    data[FOOD_SENSOR_IDX] = dataValue;
  }

  public int[] getData() {
    return data;
  }

  public void resetToDefault() {
    for (int idx = 0; idx < data.length; idx++) {
      data[idx] = 0;
    }
  }

  private SensorInfo getSensorData(int dataIdx, int entityTypeMask, int entityTypeBitIdx) {
    int dataValue = data[dataIdx];
    int entityType = ((dataValue & ~entityTypeMask) >> entityTypeBitIdx) & 0b11;
    EntityType type =
        entityType < EntityUtil.ENTITY_TYPES.length ? EntityUtil.ENTITY_TYPES[entityType] : null;
    return new SensorInfo(type);
  }

  private void setSensorData(EntityType entityType, int dataIdx, int entityTypeMask, int entityTypeBitIdx) {
    int entityTypeValue = entityType != null ? entityType.ordinal() : 0b11;

    int dataValue = data[dataIdx];
    dataValue = (dataValue & entityTypeMask) | (entityTypeValue << entityTypeBitIdx);
    data[dataIdx] = dataValue;
  }
}
