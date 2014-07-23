package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.core.AntInput.SensorInfo;

/**
 * Unit test for {@link AntInput}.
 */
public class AntInputTest {

  private Random random;
  private AntInput input;

  @Before
  public void setUp() {
    this.random = new Random(AntInputTest.class.getName().hashCode());

    int[] data = new int[AntInput.DATA_SIZE];
    for (int idx = 0; idx < data.length; idx++) {
      data[idx] = random.nextInt();
    }
    this.input = new AntInput(data);
  }

  @Test
  public void resetToDefault() {
    input.resetToDefault();
    assertEquals(0, input.getEnergyLevel());
    assertEquals(Direction.values()[0], input.getDirection());
    assertEquals(0, input.getPheromoneType());
    assertEquals(EntityType.NONE, input.getCarriedEntityType());
    assertEquals(EntityType.NONE, input.getCellSensor());
    assertEquals(Direction.values()[0], input.getFrontSensor().getDirection());
    assertEquals(0, input.getFrontSensor().getIdHash());
    assertEquals(EntityType.NONE, input.getFrontLeftSensor().getEntityType());
    assertEquals(Direction.values()[0], input.getFrontLeftSensor().getDirection());
    assertEquals(EntityType.NONE, input.getFrontRightSensor().getEntityType());
    assertEquals(Direction.values()[0], input.getFrontRightSensor().getDirection());
    assertEquals(EntityType.NONE, input.getLeftSensor().getEntityType());
    assertEquals(Direction.values()[0], input.getLeftSensor().getDirection());
    assertEquals(EntityType.NONE, input.getRightSensor().getEntityType());
    assertEquals(Direction.values()[0], input.getRightSensor().getDirection());
  }

  @Test
  public void energyLevel_negativeEnergyLevel() {
    input.setEnergy(-75, 100);
    assertEquals(0, input.getEnergyLevel());
  }

  @Test
  public void energyLevel_maxEnergy() {
    input.setEnergy(10, -100);
    assertEquals(0, input.getEnergyLevel());
  }

  @Test
  public void energyLevel() {
    input.setEnergy(0, 100);
    assertEquals(0, input.getEnergyLevel());
    input.setEnergy(25, 100);
    assertEquals(1, input.getEnergyLevel());
    input.setEnergy(35, 100);
    assertEquals(2, input.getEnergyLevel());
    input.setEnergy(50, 100);
    assertEquals(3, input.getEnergyLevel());
    input.setEnergy(65, 100);
    assertEquals(4, input.getEnergyLevel());
    input.setEnergy(75, 100);
    assertEquals(5, input.getEnergyLevel());
    input.setEnergy(90, 100);
    assertEquals(6, input.getEnergyLevel());
    input.setEnergy(100, 100);
    assertEquals(7, input.getEnergyLevel());
  }

  @Test
  public void direction() {
    for(Direction direction : Direction.values()) {
      input.setDirection(direction);
      assertEquals(direction, input.getDirection());
    }
  }

  @Test
  public void carriedEntityType() {
    for (EntityType entityType : EntityType.values()) {
      input.setCarriedEntityType(entityType);
      assertEquals(entityType, input.getCarriedEntityType());
    }
  }

  @Test
  public void pheromoneType() {
    for(int pheromoneType = 0; pheromoneType < 0b111; pheromoneType++) {
      input.setPheromoneType(pheromoneType);
      assertEquals(pheromoneType, input.getPheromoneType());
    }
  }

  @Test
  public void generalInput() {
    for (int value = 0; value < 255; value++) {
      input.setGeneralInput(value);
      assertEquals(value, input.getGeneralInput());
    }
  }

  @Test
  public void cellSensor() {
    for (EntityType entityType : EntityType.values()) {
      input.setCellSensor(entityType);
      assertEquals(entityType, input.getCellSensor());
    }
  }

  @Test
  public void frontSensor() {
    for (EntityType entityType : EntityType.values()) {
      for (Direction direction : Direction.values()) {
        String id = String.valueOf(random.nextInt());
        input.setFrontSensor(entityType, direction, id);
        SensorInfo sensorInfo = input.getFrontSensor();
        assertEquals(entityType, sensorInfo.getEntityType());
        assertEquals(direction, sensorInfo.getDirection());
        assertEquals(id.hashCode() & 0b1111, sensorInfo.getIdHash());
      }
    }
  }

  @Test
  public void frontSensor_noCell() {
    input.setFrontSensor(null, null, null);
    SensorInfo sensorInfo = input.getFrontSensor();
    assertEquals(null, sensorInfo.getEntityType());
  }

  @Test
  public void frontLeftSensor() {
    for (EntityType entityType : EntityType.values()) {
      for (Direction direction : Direction.values()) {
        input.setFrontLeftSensor(entityType, direction);
        SensorInfo sensorInfo = input.getFrontLeftSensor();
        assertEquals(entityType, sensorInfo.getEntityType());
        assertEquals(direction, sensorInfo.getDirection());
      }
    }
  }

  @Test
  public void frontLeftSensor_noCell() {
    input.setFrontLeftSensor(null, null);
    SensorInfo sensorInfo = input.getFrontLeftSensor();
    assertEquals(null, sensorInfo.getEntityType());
  }

  @Test
  public void frontRightSensor() {
    for (EntityType entityType : EntityType.values()) {
      for (Direction direction : Direction.values()) {
        input.setFrontRightSensor(entityType, direction);
        SensorInfo sensorInfo = input.getFrontRightSensor();
        assertEquals(entityType, sensorInfo.getEntityType());
        assertEquals(direction, sensorInfo.getDirection());
      }
    }
  }

  @Test
  public void frontRightSensor_noCell() {
    input.setFrontRightSensor(null, null);
    SensorInfo sensorInfo = input.getFrontRightSensor();
    assertEquals(null, sensorInfo.getEntityType());
  }

  @Test
  public void leftSensor() {
    for (EntityType entityType : EntityType.values()) {
      for (Direction direction : Direction.values()) {
        input.setLeftSensor(entityType, direction);
        SensorInfo sensorInfo = input.getLeftSensor();
        assertEquals(entityType, sensorInfo.getEntityType());
        assertEquals(direction, sensorInfo.getDirection());
      }
    }
  }

  @Test
  public void leftSensor_noCell() {
    input.setLeftSensor(null, null);
    SensorInfo sensorInfo = input.getLeftSensor();
    assertEquals(null, sensorInfo.getEntityType());
  }

  @Test
  public void rightSensor() {
    for (EntityType entityType : EntityType.values()) {
      for (Direction direction : Direction.values()) {
        input.setRightSensor(entityType, direction);
        SensorInfo sensorInfo = input.getRightSensor();
        assertEquals(entityType, sensorInfo.getEntityType());
        assertEquals(direction, sensorInfo.getDirection());
      }
    }
  }

  @Test
  public void rightSensor_noCell() {
    input.setRightSensor(null, null);
    SensorInfo sensorInfo = input.getRightSensor();
    assertEquals(null, sensorInfo.getEntityType());
  }

  @Test
  public void dataSize() {
    AntInput input = new AntInput();
    assertEquals(AntInput.DATA_SIZE, input.getData().length);
  }
}
