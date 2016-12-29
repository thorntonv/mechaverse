package org.mechaverse.simulation.primordial.core.entity.primordial;

import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;
import org.mechaverse.simulation.common.util.SimulationModelUtil;

/**
 * Encodes and decodes input for a primordial entity.
 */
public final class PrimordialEntityInput {

  /**
   * Sensor information about an entity in a cell.
   */
  public static final class SensorInfo {

    private EntityType entityType;
    private Direction direction;
    private int idHash;

    public SensorInfo(EntityType entityType, Direction direction, int idHash) {
      super();
      this.entityType = entityType;
      this.direction = direction;
      this.idHash = idHash;
    }

    public EntityType getEntityType() {
      return entityType;
    }

    public Direction getDirection() {
      return direction;
    }

    public int getIdHash() {
      return idHash;
    }
  }

  public static int DATA_SIZE = 4;

  private static final int ENERGY_LEVEL_IDX = 0;
  private static final int ENERGY_LEVEL_MASK = ~0b111;

  private static final int DIRECTION_IDX = 0;
  private static final int DIRECTION_BIT_IDX = 3;
  private static final int DIRECTION_MASK = ~(0b111 << DIRECTION_BIT_IDX);

  private static final int FRONT_SENSOR_IDX = 0;
  private static final int FRONT_ENTITY_TYPE_BIT_IDX = 10;
  private static final int FRONT_ENTITY_TYPE_MASK = ~(0b1111 << FRONT_ENTITY_TYPE_BIT_IDX);
  private static final int FRONT_ENTITY_DIRECTION_BIT_IDX = 14;
  private static final int FRONT_ENTITY_DIRECTION_MASK = ~(0b111 << FRONT_ENTITY_DIRECTION_BIT_IDX);
  private static final int FRONT_ENTITY_ID_BIT_IDX = 17;
  private static final int FRONT_ENTITY_ID_MASK = ~(0b1111 << FRONT_ENTITY_ID_BIT_IDX);

  // IDX = 1

  private static final int FRONT_LEFT_SENSOR_IDX = 1;
  private static final int FRONT_LEFT_ENTITY_TYPE_BIT_IDX = 4;
  private static final int FRONT_LEFT_ENTITY_TYPE_MASK =
      ~(0b1111 << FRONT_LEFT_ENTITY_TYPE_BIT_IDX);
  private static final int FRONT_LEFT_ENTITY_DIRECTION_BIT_IDX = 8;
  private static final int FRONT_LEFT_ENTITY_DIRECTION_MASK =
      ~(0b111 << FRONT_LEFT_ENTITY_DIRECTION_BIT_IDX);

  private static final int FRONT_RIGHT_SENSOR_IDX = 1;
  private static final int FRONT_RIGHT_ENTITY_TYPE_BIT_IDX = 11;
  private static final int FRONT_RIGHT_ENTITY_TYPE_MASK =
      ~(0b1111 << FRONT_RIGHT_ENTITY_TYPE_BIT_IDX);
  private static final int FRONT_RIGHT_ENTITY_DIRECTION_BIT_IDX = 15;
  private static final int FRONT_RIGHT_ENTITY_DIRECTION_MASK =
      ~(0b111 << FRONT_RIGHT_ENTITY_DIRECTION_BIT_IDX);

  private static final int LEFT_SENSOR_IDX = 1;
  private static final int LEFT_ENTITY_TYPE_BIT_IDX = 18;
  private static final int LEFT_ENTITY_TYPE_MASK = ~(0b1111 << LEFT_ENTITY_TYPE_BIT_IDX);
  private static final int LEFT_ENTITY_DIRECTION_BIT_IDX = 22;
  private static final int LEFT_ENTITY_DIRECTION_MASK = ~(0b111 << LEFT_ENTITY_DIRECTION_BIT_IDX);

  private static final int RIGHT_SENSOR_IDX = 1;
  private static final int RIGHT_ENTITY_TYPE_BIT_IDX = 25;
  private static final int RIGHT_ENTITY_TYPE_MASK = ~(0b1111 << RIGHT_ENTITY_TYPE_BIT_IDX);
  private static final int RIGHT_ENTITY_DIRECTION_BIT_IDX = 29;
  private static final int RIGHT_ENTITY_DIRECTION_MASK = ~(0b111 << RIGHT_ENTITY_DIRECTION_BIT_IDX);

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
   * Sets the energy level for the entity. It is represented using 3 bits (0 = low, 7 = high).
   */
  public void setEnergy(int energyLevel, int maxEnergyLevel) {
    // Use a direct proportion to map the energy level on to the new range.
    int value = (energyLevel > 0 && maxEnergyLevel > 0) ? energyLevel * 7 / maxEnergyLevel : 0;
    data[ENERGY_LEVEL_IDX] = (data[ENERGY_LEVEL_IDX] & ENERGY_LEVEL_MASK) | value;
  }

  public void setDirection(Direction direction) {
    int value = direction.ordinal();
    data[DIRECTION_IDX] = (data[DIRECTION_IDX] & DIRECTION_MASK) | (value << DIRECTION_BIT_IDX);
  }

  public Direction getDirection() {
    int direction = (data[DIRECTION_IDX] & ~DIRECTION_MASK) >> DIRECTION_BIT_IDX;
    return SimulationModelUtil.DIRECTIONS[direction];
  }

  public SensorInfo getFrontSensor() {
    return getSensorData(FRONT_SENSOR_IDX, FRONT_ENTITY_TYPE_MASK, FRONT_ENTITY_TYPE_BIT_IDX,
        FRONT_ENTITY_DIRECTION_MASK, FRONT_ENTITY_DIRECTION_BIT_IDX,
        FRONT_ENTITY_ID_MASK, FRONT_ENTITY_ID_BIT_IDX);
  }

  public void setFrontSensor(EntityType entityType, Direction entityDirection, String entityId) {
    setSensorData(entityType, entityDirection, entityId, FRONT_SENSOR_IDX,
        FRONT_ENTITY_TYPE_MASK, FRONT_ENTITY_TYPE_BIT_IDX,
        FRONT_ENTITY_DIRECTION_MASK, FRONT_ENTITY_DIRECTION_BIT_IDX,
        FRONT_ENTITY_ID_MASK, FRONT_ENTITY_ID_BIT_IDX);
  }

  public SensorInfo getFrontLeftSensor() {
    return getSensorData(FRONT_LEFT_SENSOR_IDX,
        FRONT_LEFT_ENTITY_TYPE_MASK, FRONT_LEFT_ENTITY_TYPE_BIT_IDX,
        FRONT_LEFT_ENTITY_DIRECTION_MASK, FRONT_LEFT_ENTITY_DIRECTION_BIT_IDX);
  }

  public void setFrontLeftSensor(EntityType entityType, Direction entityDirection) {
    setSensorData(entityType, entityDirection, FRONT_LEFT_SENSOR_IDX,
        FRONT_LEFT_ENTITY_TYPE_MASK, FRONT_LEFT_ENTITY_TYPE_BIT_IDX,
        FRONT_LEFT_ENTITY_DIRECTION_MASK, FRONT_LEFT_ENTITY_DIRECTION_BIT_IDX);
  }

  public SensorInfo getLeftSensor() {
    return getSensorData(LEFT_SENSOR_IDX,
        LEFT_ENTITY_TYPE_MASK, LEFT_ENTITY_TYPE_BIT_IDX,
        LEFT_ENTITY_DIRECTION_MASK, LEFT_ENTITY_DIRECTION_BIT_IDX);
  }

  public void setLeftSensor(EntityType entityType, Direction entityDirection) {
    setSensorData(entityType, entityDirection, LEFT_SENSOR_IDX,
        LEFT_ENTITY_TYPE_MASK, LEFT_ENTITY_TYPE_BIT_IDX,
        LEFT_ENTITY_DIRECTION_MASK, LEFT_ENTITY_DIRECTION_BIT_IDX);
  }

  public SensorInfo getFrontRightSensor() {
    return getSensorData(FRONT_RIGHT_SENSOR_IDX,
        FRONT_RIGHT_ENTITY_TYPE_MASK, FRONT_RIGHT_ENTITY_TYPE_BIT_IDX,
        FRONT_RIGHT_ENTITY_DIRECTION_MASK, FRONT_RIGHT_ENTITY_DIRECTION_BIT_IDX);
  }

  public void setFrontRightSensor(EntityType entityType, Direction entityDirection) {
    setSensorData(entityType, entityDirection, FRONT_RIGHT_SENSOR_IDX,
        FRONT_RIGHT_ENTITY_TYPE_MASK, FRONT_RIGHT_ENTITY_TYPE_BIT_IDX,
        FRONT_RIGHT_ENTITY_DIRECTION_MASK, FRONT_RIGHT_ENTITY_DIRECTION_BIT_IDX);
  }

  public SensorInfo getRightSensor() {
    return getSensorData(RIGHT_SENSOR_IDX,
        RIGHT_ENTITY_TYPE_MASK, RIGHT_ENTITY_TYPE_BIT_IDX,
        RIGHT_ENTITY_DIRECTION_MASK, RIGHT_ENTITY_DIRECTION_BIT_IDX);
  }

  public void setRightSensor(EntityType entityType, Direction entityDirection) {
    setSensorData(entityType, entityDirection, RIGHT_SENSOR_IDX,
        RIGHT_ENTITY_TYPE_MASK, RIGHT_ENTITY_TYPE_BIT_IDX,
        RIGHT_ENTITY_DIRECTION_MASK, RIGHT_ENTITY_DIRECTION_BIT_IDX);
  }

  public int[] getData() {
    return data;
  }

  public void resetToDefault() {
    for (int idx = 0; idx < data.length; idx++) {
      data[idx] = 0;
    }
  }

  private SensorInfo getSensorData(int dataIdx, int entityTypeMask, int entityTypeBitIdx,
      int directionMask, int directionBitIdx) {
    int dataValue = data[dataIdx];
    int entityType = ((dataValue & ~entityTypeMask) >> entityTypeBitIdx) & 0b1111;
    int direction = (dataValue & ~directionMask) >> directionBitIdx;
    EntityType type = entityType < EntityUtil.ENTITY_TYPES.length
        ? EntityUtil.ENTITY_TYPES[entityType] : null;
    return new SensorInfo(type, SimulationModelUtil.DIRECTIONS[direction & 0b111], 0);
  }

  private SensorInfo getSensorData(int dataIdx, int entityTypeMask, int entityTypeBitIdx,
      int directionMask, int directionBitIdx, int idMask, int idBitIdx) {
    int dataValue = data[dataIdx];
    int entityType = ((dataValue & ~entityTypeMask) >> entityTypeBitIdx) & 0b1111;
    int direction = (dataValue & ~directionMask) >> directionBitIdx;
    int idHash = (dataValue & ~idMask) >> idBitIdx;
    EntityType type = entityType < EntityUtil.ENTITY_TYPES.length
        ? EntityUtil.ENTITY_TYPES[entityType] : null;
    return new SensorInfo(type, SimulationModelUtil.DIRECTIONS[direction & 0b111], idHash & 0b1111);
  }

  private void setSensorData(EntityType entityType, Direction entityDirection, int dataIdx,
      int entityTypeMask, int entityTypeBitIdx, int directionMask, int directionBitIdx) {
    int entityTypeValue = entityType != null ? entityType.ordinal() : 0b1111;
    int directionValue = entityDirection != null ? entityDirection.ordinal() : 0;

    int dataValue = data[dataIdx];
    dataValue = (dataValue & entityTypeMask) | (entityTypeValue << entityTypeBitIdx);
    dataValue = (dataValue & directionMask) | (directionValue << directionBitIdx);
    data[dataIdx] = dataValue;
  }

  private void setSensorData(EntityType entityType, Direction entityDirection, String entityId,
      int dataIdx, int entityTypeMask, int entityTypeBitIdx, int directionMask, int directionBitIdx,
          int idMask, int idBitIdx) {
    int entityTypeValue = entityType != null ? entityType.ordinal() : 0b1111;
    int directionValue = entityDirection != null ? entityDirection.ordinal() : 0;
    int idHashValue = entityId != null ? entityId.hashCode() & 0b1111 : 0;

    int dataValue = data[dataIdx];
    dataValue = (dataValue & entityTypeMask) | (entityTypeValue << entityTypeBitIdx);
    dataValue = (dataValue & directionMask) | (directionValue << directionBitIdx);
    dataValue = (dataValue & idMask) | (idHashValue  << idBitIdx);
    data[dataIdx] = dataValue;
  }
}
