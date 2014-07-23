package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Food;
import org.mechaverse.simulation.ant.api.model.Pheromone;
import org.mechaverse.simulation.ant.api.model.Rock;
import org.mechaverse.simulation.ant.core.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.ant.core.AntInput.SensorInfo;
import org.mechaverse.simulation.ant.core.AntOutput.MoveDirection;
import org.mechaverse.simulation.ant.core.AntOutput.TurnDirection;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link ActiveAnt}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ActiveAntTest {

  @Mock Ant mockAntEntity;
  @Mock AntBehavior mockAntBehavior;
  @Mock CellEnvironment mockEnvironment;
  @Mock Cell mockCell;
  @Mock Cell mockFrontCell;
  @Mock Cell mockOtherCell;
  @Mock Ant mockAnt;
  @Mock Food mockFood;
  @Mock Pheromone mockPheromone;
  @Mock Rock mockRock;

  @Captor ArgumentCaptor<AntInput> inputCaptor;

  private ActiveAnt activeAnt;

  @Before
  public void setUp() {
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);

    when(mockAntEntity.getDirection()).thenReturn(Direction.EAST);
    when(mockEnvironment.getCell(mockAntEntity)).thenReturn(mockCell);
    when(mockEnvironment.getCellInDirection(mockCell, Direction.EAST)).thenReturn(mockFrontCell);
  }

  @Test
  public void getEntity() {
    assertEquals(mockAntEntity, activeAnt.getEntity());
  }

  @Test
  public void getEntityType() {
    assertEquals(EntityType.ANT, activeAnt.getType());
  }

  @Test
  public void energyLevel() {
    when(mockAntEntity.getEnergy()).thenReturn(75);
    when(mockAntEntity.getMaxEnergy()).thenReturn(100);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(5, inputCaptor.getValue().getEnergyLevel());
  }

  @Test
  public void direction() {
    when(mockAntEntity.getDirection()).thenReturn(Direction.SOUTH_WEST);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(Direction.SOUTH_WEST, inputCaptor.getValue().getDirection());
  }

  @Test
  public void cellSensor_empty() {
    when(mockCell.getEntity(EntityType.ANT)).thenReturn(mockAntEntity);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(EntityType.NONE, inputCaptor.getValue().getCellSensor());
  }

  @Test
  public void cellSensor_food() {
    when(mockCell.getEntity(EntityType.FOOD)).thenReturn(mockFood);
    when(mockCell.getEntity(EntityType.ANT)).thenReturn(mockAntEntity);

    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());

    assertEquals(EntityType.FOOD, inputCaptor.getValue().getCellSensor());
  }

  @Test
  public void frontCellSensor_empty() {
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(EntityType.NONE, inputCaptor.getValue().getFrontSensor().getEntityType());
  }

  @Test
  public void frontCellSensor_noCell() {
    when(mockEnvironment.getCellInDirection(mockCell, Direction.EAST)).thenReturn(null);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(null, inputCaptor.getValue().getFrontSensor().getEntityType());
  }

  @Test
  public void frontCellSensor_ant() {
    when(mockFrontCell.getEntity()).thenReturn(mockAnt);
    when(mockFrontCell.getEntityType()).thenReturn(EntityType.ANT);
    when(mockAnt.getDirection()).thenReturn(Direction.WEST);
    when(mockAnt.getId()).thenReturn("test");

    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());

    verifySensorInfo(inputCaptor.getValue().getFrontSensor(),
        EntityType.ANT, Direction.WEST, "test".hashCode());
  }

  @Test
  public void frontCellSensor_food() {
    when(mockFrontCell.getEntity()).thenReturn(mockFood);
    when(mockFrontCell.getEntityType()).thenReturn(EntityType.FOOD);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(EntityType.FOOD, inputCaptor.getValue().getFrontSensor().getEntityType());
  }

  @Test
  public void frontLeftCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.NORTH_EAST);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(EntityType.FOOD, inputCaptor.getValue().getFrontLeftSensor().getEntityType());
  }

  @Test
  public void frontLeftCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.NORTH_EAST);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(EntityType.NONE, inputCaptor.getValue().getFrontLeftSensor().getEntityType());
  }

  @Test
  public void frontLeftCellSensor_noCell() {
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(null, inputCaptor.getValue().getFrontLeftSensor().getEntityType());
  }

  @Test
  public void frontRightCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.SOUTH_EAST);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(EntityType.FOOD, inputCaptor.getValue().getFrontRightSensor().getEntityType());
  }

  @Test
  public void frontRightCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.SOUTH_EAST);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(EntityType.NONE, inputCaptor.getValue().getFrontRightSensor().getEntityType());
  }

  @Test
  public void frontRightCellSensor_noCell() {
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(null, inputCaptor.getValue().getFrontRightSensor().getEntityType());
  }

  @Test
  public void leftCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.NORTH);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(EntityType.FOOD, inputCaptor.getValue().getLeftSensor().getEntityType());
  }

  @Test
  public void leftCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.NORTH);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(EntityType.NONE, inputCaptor.getValue().getLeftSensor().getEntityType());
  }

  @Test
  public void leftCellSensor_noCell() {
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(null, inputCaptor.getValue().getLeftSensor().getEntityType());
  }

  @Test
  public void rightCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.SOUTH);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(EntityType.FOOD, inputCaptor.getValue().getRightSensor().getEntityType());
  }

  @Test
  public void rightCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.SOUTH);
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(EntityType.NONE, inputCaptor.getValue().getRightSensor().getEntityType());
  }

  @Test
  public void rightCellSensor_noCell() {
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(null, inputCaptor.getValue().getRightSensor().getEntityType());
  }

  @Test
  public void pheromoneSensor_noPheromone() {
    activeAnt.updateInput(mockEnvironment);
    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(0, inputCaptor.getValue().getPheromoneType());
  }

  @Test
  public void pheromoneSensor() {
    when(mockCell.getEntity(EntityType.PHEROMONE)).thenReturn(mockPheromone);
    when(mockPheromone.getValue()).thenReturn(3);
    activeAnt.updateInput(mockEnvironment);

    verify(mockAntBehavior).setInput(inputCaptor.capture());
    assertEquals(3, inputCaptor.getValue().getPheromoneType());
  }

  @Test
  public void moveForward_emptyCell() {
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockEnvironment).moveEntityToCell(EntityType.ANT, mockCell, mockFrontCell);
  }

  @Test
  public void moveForward_nonExistentCell() {
    when(mockEnvironment.getCellInDirection(mockCell, Direction.EAST)).thenReturn(null);
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockEnvironment, never()).moveEntityToCell(
        any(EntityType.class), any(Cell.class), any(Cell.class));
  }

  @Test
  public void moveForward_rockCell() {
    when(mockFrontCell.hasEntity(EntityType.ROCK)).thenReturn(true);
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockEnvironment, never()).moveEntityToCell(
        any(EntityType.class), any(Cell.class), any(Cell.class));
  }

  @Test
  public void moveForward_barrierCell() {
    when(mockFrontCell.hasEntity(EntityType.BARRIER)).thenReturn(true);
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockEnvironment, never()).moveEntityToCell(
        any(EntityType.class), any(Cell.class), any(Cell.class));
  }

  @Test
  public void moveForward_carrying() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockFood);
    when(mockAntEntity.getX()).thenReturn(123);
    when(mockAntEntity.getY()).thenReturn(321);
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);

    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockFood).setX(123);
    verify(mockFood).setY(321);
    verify(mockEnvironment).moveEntityToCell(EntityType.ANT, mockCell, mockFrontCell);
  }

  @Test
  public void turnDirection_clockwise() {
    AntOutput output = new AntOutput();
    output.setTurnDirection(TurnDirection.CLOCKWISE);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockAntEntity).setDirection(Direction.SOUTH_EAST);
  }

  @Test
  public void turnDirection_counterClockwise() {
    AntOutput output = new AntOutput();
    output.setTurnDirection(TurnDirection.COUNTERCLOCKWISE);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockAntEntity).setDirection(Direction.NORTH_EAST);
  }

  @Test
  public void turnDirection_none() {
    AntOutput output = new AntOutput();
    output.setTurnDirection(TurnDirection.NONE);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockAntEntity, never()).setDirection(any(Direction.class));
  }

  @Test
  public void pickup_food() {
    when(mockCell.getEntity(EntityType.FOOD)).thenReturn(mockFood);
    AntOutput output = new AntOutput();
    output.setPickUp(true);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockCell).removeEntity(EntityType.FOOD);
    verify(mockAntEntity).setCarriedEntity(mockFood);
  }

  @Test
  public void pickup_rock() {
    when(mockFrontCell.getEntity(EntityType.ROCK)).thenReturn(mockRock);
    AntOutput output = new AntOutput();
    output.setPickUp(true);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockFrontCell).removeEntity(EntityType.ROCK);
    verify(mockAntEntity).setCarriedEntity(mockRock);
  }

  @Test
  public void pickup_empty() {
    AntOutput output = new AntOutput();
    output.setPickUp(true);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockCell, never()).removeEntity(any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(any(Entity.class));
  }

  @Test
  public void pickup_alreadyCarrying() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockFood);
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);

    AntOutput output = new AntOutput();
    output.setPickUp(true);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockCell, never()).removeEntity(any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(any(Entity.class));
  }

  @Test
  public void pickup_pheromone() {
    when(mockCell.getEntity(EntityType.PHEROMONE)).thenReturn(mockPheromone);
    AntOutput output = new AntOutput();
    output.setPickUp(true);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockCell, never()).removeEntity(any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(any(Entity.class));
  }

  @Test
  public void drop_notCarrying() {
    AntOutput output = new AntOutput();
    output.setDrop(true);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockCell, never()).setEntity(any(Entity.class), any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(any(Entity.class));
  }

  @Test
  public void drop_food() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockFood);
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);
    AntOutput output = new AntOutput();
    output.setDrop(true);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockCell).setEntity(mockFood, EntityType.FOOD);
    verify(mockAntEntity).setCarriedEntity(null);
  }

  @Test
  public void drop_rock() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockRock);
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);
    AntOutput output = new AntOutput();
    output.setDrop(true);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockFrontCell).setEntity(mockRock, EntityType.ROCK);
    verify(mockAntEntity).setCarriedEntity(null);
  }

  @Test
  public void drop_onOccupiedCell() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockFood);
    when(mockCell.getEntity(EntityType.FOOD)).thenReturn(mockFood);
    when(mockFrontCell.getEntity(EntityType.FOOD)).thenReturn(mockFood);
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);
    AntOutput output = new AntOutput();
    output.setDrop(true);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockCell, never()).setEntity(any(Entity.class), any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(any(Entity.class));
  }

  @Test
  public void drop_atEnvironmentBorder() {
    when(mockEnvironment.getCellInDirection(mockCell, Direction.EAST)).thenReturn(null);
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockRock);
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);
    AntOutput output = new AntOutput();
    output.setDrop(true);
    when(mockAntBehavior.getOutput()).thenReturn(output);

    activeAnt.performAction(mockEnvironment);

    verify(mockCell, never()).setEntity(any(Entity.class), any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(any(Entity.class));
  }

  private void mockEntityAtCellInDirection(
      EntityType entityType, Entity entity, Cell cell, Direction direction) {
    when(mockEnvironment.getCellInDirection(cell, direction)).thenReturn(mockOtherCell);
    when(mockOtherCell.getEntity()).thenReturn(entity);
    when(mockOtherCell.getEntityType()).thenReturn(entityType);
  }

  private void verifySensorInfo(SensorInfo sensorInfo,
      EntityType entityType, Direction direction, int idHashCode) {
    assertEquals(entityType, sensorInfo.getEntityType());
    assertEquals(direction, sensorInfo.getDirection());
    assertEquals(idHashCode & 0b1111, sensorInfo.getIdHash());
  }
}