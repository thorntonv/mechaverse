package org.mechaverse.simulation.ant.core.entity.ant;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.model.Food;
import org.mechaverse.simulation.ant.core.model.Nest;
import org.mechaverse.simulation.ant.core.model.Pheromone;
import org.mechaverse.simulation.ant.core.model.Rock;
import org.mechaverse.simulation.ant.core.AntSimulationTestUtil;
import org.mechaverse.simulation.ant.core.model.Cell;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.ant.core.entity.ant.AntInput.SensorInfo;
import org.mechaverse.simulation.common.model.MoveDirection;
import org.mechaverse.simulation.common.model.TurnDirection;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link ActiveAnt}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ActiveAntTest {

  @Mock
  private Ant mockAntEntity;
  @Mock
  private AntBehavior mockAntBehavior;
  @Mock
  private CellEnvironment mockEnvironment;
  @Mock
  private Cell mockCell;
  @Mock
  private Cell mockFrontCell;
  @Mock
  private Cell mockOtherCell;
  @Mock
  private Ant mockAnt;
  @Mock
  private Food mockFood;
  @Mock
  private Nest mockNest;
  @Mock
  private Pheromone mockPheromone;
  @Mock
  private Rock mockRock;
  @Mock
  private EntityManager mockEntityManager;

  @Captor
  private ArgumentCaptor<AntInput> inputCaptor;

  private ActiveAnt activeAnt;
  private RandomGenerator random;

  @Before
  public void setUp() {
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);
    random = RandomUtil.newGenerator(ActiveAntTest.class.getName().hashCode());

    when(mockAntEntity.getId()).thenReturn("001");
    when(mockAntEntity.getEnergy()).thenReturn(10);
    when(mockAntEntity.getDirection()).thenReturn(Direction.EAST);
    when(mockEnvironment.getCell(mockAntEntity)).thenReturn(mockCell);
    when(mockEnvironment.getCellInDirection(mockCell, Direction.EAST)).thenReturn(mockFrontCell);
    when(mockEnvironment.getNestDirection(any(Cell.class))).thenReturn(Direction.EAST);
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
  public void age() {
    when(mockAntEntity.getAge()).thenReturn(100L);
    mockOutput(new AntOutput());
    activeAnt.performAction(mockEnvironment, mockEntityManager, random);
    verify(mockAntEntity).setAge(101L);
  }

  @Test
  public void energyLevel() {
    when(mockAntEntity.getEnergy()).thenReturn(75);
    when(mockAntEntity.getMaxEnergy()).thenReturn(100);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(5, inputCaptor.getValue().getEnergyLevel());
  }

  @Test
  public void performAction_energyConsumed() {
    mockOutput(new AntOutput());
    activeAnt.performAction(mockEnvironment, mockEntityManager, random);
    verify(mockAntEntity).setEnergy(9);
  }

  @Test
  public void performAction_noEnergy() {
    mockOutput(new AntOutput());
    when(mockAntEntity.getEnergy()).thenReturn(1).thenReturn(0);
    activeAnt.performAction(mockEnvironment, mockEntityManager, random);
    verify(mockAntEntity).setEnergy(0);
    verify(mockEntityManager).removeEntity(activeAnt.getEntity());
    verify(mockAntBehavior).onRemoveEntity();
  }

  @Test
  public void performAction_noEnergy_carriedEntity() {
    mockOutput(new AntOutput());
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockRock);
    when(mockAntEntity.getEnergy()).thenReturn(1).thenReturn(0);
    when(mockEnvironment.getCell(mockAntEntity)).thenReturn(mockCell);
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);
    activeAnt.performAction(mockEnvironment, mockEntityManager, random);
    verify(mockAntEntity).setEnergy(0);
    verify(mockEntityManager).removeEntity(activeAnt.getEntity());
    verify(mockAntBehavior).onRemoveEntity();
    verify(mockCell).setEntity(mockRock, EntityType.ROCK);
  }

  @Test
  public void direction() {
    when(mockAntEntity.getDirection()).thenReturn(Direction.SOUTH_WEST);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(Direction.SOUTH_WEST, inputCaptor.getValue().getDirection());
  }

  @Test
  public void cellSensor_empty() {
    when(mockCell.getEntity(EntityType.ANT)).thenReturn(mockAntEntity);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(EntityType.NONE, inputCaptor.getValue().getCellSensor());
  }

  @Test
  public void cellSensor_food() {
    when(mockCell.getEntity(EntityType.FOOD)).thenReturn(mockFood);
    when(mockCell.getEntity(EntityType.ANT)).thenReturn(mockAntEntity);

    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));

    assertEquals(EntityType.FOOD, inputCaptor.getValue().getCellSensor());
  }

  @Test
  public void frontCellSensor_empty() {
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(EntityType.NONE, inputCaptor.getValue().getFrontSensor().getEntityType());
  }

  @Test
  public void frontCellSensor_noCell() {
    when(mockEnvironment.getCellInDirection(mockCell, Direction.EAST)).thenReturn(null);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertNull(inputCaptor.getValue().getFrontSensor().getEntityType());
  }

  @Test
  public void frontCellSensor_ant() {
    when(mockFrontCell.getEntity()).thenReturn(mockAnt);
    when(mockFrontCell.getEntityType()).thenReturn(EntityType.ANT);
    when(mockAnt.getDirection()).thenReturn(Direction.WEST);
    when(mockAnt.getId()).thenReturn("test");

    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));

    verifySensorInfo(inputCaptor.getValue().getFrontSensor(),
        EntityType.ANT, Direction.WEST, "test".hashCode());
  }

  @Test
  public void frontCellSensor_food() {
    when(mockFrontCell.getEntity()).thenReturn(mockFood);
    when(mockFrontCell.getEntityType()).thenReturn(EntityType.FOOD);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(EntityType.FOOD, inputCaptor.getValue().getFrontSensor().getEntityType());
  }

  @Test
  public void frontLeftCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.NORTH_EAST);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(EntityType.FOOD, inputCaptor.getValue().getFrontLeftSensor().getEntityType());
  }

  @Test
  public void frontLeftCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.NORTH_EAST);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(EntityType.NONE, inputCaptor.getValue().getFrontLeftSensor().getEntityType());
  }

  @Test
  public void frontLeftCellSensor_noCell() {
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertNull(inputCaptor.getValue().getFrontLeftSensor().getEntityType());
  }

  @Test
  public void frontRightCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.SOUTH_EAST);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(EntityType.FOOD, inputCaptor.getValue().getFrontRightSensor().getEntityType());
  }

  @Test
  public void frontRightCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.SOUTH_EAST);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(EntityType.NONE, inputCaptor.getValue().getFrontRightSensor().getEntityType());
  }

  @Test
  public void frontRightCellSensor_noCell() {
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertNull(inputCaptor.getValue().getFrontRightSensor().getEntityType());
  }

  @Test
  public void leftCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.NORTH);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(EntityType.FOOD, inputCaptor.getValue().getLeftSensor().getEntityType());
  }

  @Test
  public void leftCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.NORTH);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(EntityType.NONE, inputCaptor.getValue().getLeftSensor().getEntityType());
  }

  @Test
  public void leftCellSensor_noCell() {
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertNull(inputCaptor.getValue().getLeftSensor().getEntityType());
  }

  @Test
  public void rightCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.SOUTH);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(EntityType.FOOD, inputCaptor.getValue().getRightSensor().getEntityType());
  }

  @Test
  public void rightCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.SOUTH);
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(EntityType.NONE, inputCaptor.getValue().getRightSensor().getEntityType());
  }

  @Test
  public void rightCellSensor_noCell() {
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertNull(inputCaptor.getValue().getRightSensor().getEntityType());
  }

  @Test
  public void pheromoneSensor_noPheromone() {
    activeAnt.updateInput(mockEnvironment, random);
    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(0, inputCaptor.getValue().getPheromoneType());
  }

  @Test
  public void pheromoneSensor() {
    when(mockCell.getEntity(EntityType.PHEROMONE)).thenReturn(mockPheromone);
    when(mockPheromone.getValue()).thenReturn(3);
    activeAnt.updateInput(mockEnvironment, random);

    verify(mockAntBehavior).setInput(inputCaptor.capture(), eq(random));
    assertEquals(3, inputCaptor.getValue().getPheromoneType());
  }

  @Test
  public void moveForward_emptyCell() {
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockEnvironment).moveEntityToCell(EntityType.ANT, mockCell, mockFrontCell);
  }

  @Test
  public void moveForward_nonExistentCell() {
    when(mockEnvironment.getCellInDirection(mockCell, Direction.EAST)).thenReturn(null);
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockEnvironment, never()).moveEntityToCell(
        any(EntityType.class), any(Cell.class), any(Cell.class));
  }

  @Test
  public void moveForward_rockCell() {
    when(mockFrontCell.hasEntity(EntityType.ROCK)).thenReturn(true);
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockEnvironment, never()).moveEntityToCell(
        any(EntityType.class), any(Cell.class), any(Cell.class));
  }

  @Test
  public void moveForward_barrierCell() {
    when(mockFrontCell.hasEntity(EntityType.BARRIER)).thenReturn(true);
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

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
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockFood).setX(123);
    verify(mockFood).setY(321);
    verify(mockEnvironment).moveEntityToCell(EntityType.ANT, mockCell, mockFrontCell);
  }

  @Test
  public void turnDirection_clockwise() {
    AntOutput output = new AntOutput();
    output.setTurnDirection(TurnDirection.CLOCKWISE);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockAntEntity, times(1)).setDirection(any(Direction.class));
    verify(mockAntEntity).setDirection(Direction.SOUTH_EAST);
  }

  @Test
  public void turnDirection_counterClockwise() {
    AntOutput output = new AntOutput();
    output.setTurnDirection(TurnDirection.COUNTERCLOCKWISE);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockAntEntity).setDirection(any(Direction.class));
    verify(mockAntEntity).setDirection(Direction.NORTH_EAST);
  }

  @Test
  public void turnDirection_none() {
    AntOutput output = new AntOutput();
    output.setTurnDirection(TurnDirection.NONE);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockAntEntity, never()).setDirection(any(Direction.class));
  }

  @Test
  public void pickup_food() {
    when(mockCell.getEntity(EntityType.FOOD)).thenReturn(mockFood);
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockCell).removeEntity(EntityType.FOOD);
    verify(mockAntEntity).setCarriedEntity(mockFood);
  }

  @Test
  public void pickup_rock() {
    when(mockAntEntity.getX()).thenReturn(25);
    when(mockAntEntity.getY()).thenReturn(38);
    when(mockFrontCell.getEntity(EntityType.ROCK)).thenReturn(mockRock);
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockFrontCell).removeEntity(EntityType.ROCK);
    verify(mockAntEntity).setCarriedEntity(mockRock);
    verify(mockRock).setX(25);
    verify(mockRock).setY(38);
  }

  @Test
  public void pickup_empty() {
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockCell, never()).removeEntity(any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(any(EntityModel.class));
  }

  @Test
  public void pickup_pheromone() {
    when(mockCell.getEntity(EntityType.PHEROMONE)).thenReturn(mockPheromone);
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockCell, never()).removeEntity(any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(any(EntityModel.class));
  }

  @Test
  public void drop_notCarrying() {
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockCell, never()).setEntity(any(EntityModel.class), any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(any(EntityModel.class));
  }

  @Test
  public void drop_food() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockFood);
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockCell).setEntity(mockFood, EntityType.FOOD);
    verify(mockAntEntity).setCarriedEntity(null);
  }

  @Test
  public void drop_rock() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockRock);
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

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
    output.setPickUpOrDrop(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockCell, never()).setEntity(any(EntityModel.class), any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(any(EntityModel.class));
  }

  @Test
  public void drop_atEnvironmentBorder() {
    when(mockEnvironment.getCellInDirection(mockCell, Direction.EAST)).thenReturn(null);
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockRock);
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockCell, never()).setEntity(any(EntityModel.class), any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(any(EntityModel.class));
  }

  @Test
  public void leavePheromone() {
    when(mockCell.getColumn()).thenReturn(123);
    when(mockCell.getRow()).thenReturn(321);
    AntOutput output = new AntOutput();
    output.setLeavePheromone(true);
    output.setPheromoneType(4);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    ArgumentCaptor<Pheromone> pheromoneCaptor = ArgumentCaptor.forClass(Pheromone.class);
    verify(mockCell).setEntity(pheromoneCaptor.capture(), eq(EntityType.PHEROMONE));
    verify(mockEntityManager).addEntity(pheromoneCaptor.getValue());
    assertEquals(4, pheromoneCaptor.getValue().getValue());
  }

  @Test
  public void leavePheromone_replaceExisting() {
    when(mockCell.getEntity(EntityType.PHEROMONE)).thenReturn(mockPheromone);
    leavePheromone();
  }

  @Test
  public void consume_none() {
    when(mockAntEntity.getMaxEnergy()).thenReturn(100);
    when(mockAntEntity.getEnergy()).thenReturn(50);

    AntOutput output = new AntOutput();
    output.setConsume(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockAntEntity).setEnergy(49);
  }

  @Test
  public void consume_food() {
    when(mockCell.getEntity(EntityType.FOOD)).thenReturn(mockFood);
    when(mockAntEntity.getMaxEnergy()).thenReturn(100);
    when(mockAntEntity.getEnergy()).thenReturn(50);
    when(mockFood.getEnergy()).thenReturn(40);

    AntOutput output = new AntOutput();
    output.setConsume(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockAntEntity).setEnergy(90);
    verify(mockEntityManager).removeEntity(mockFood);
  }

  @Test
  public void consume_carriedFood() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockFood);
    activeAnt = new ActiveAnt(mockAntEntity, mockAntBehavior);
    when(mockAntEntity.getMaxEnergy()).thenReturn(100);
    when(mockAntEntity.getEnergy()).thenReturn(50);
    when(mockFood.getEnergy()).thenReturn(45);

    AntOutput output = new AntOutput();
    output.setConsume(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockAntEntity).setEnergy(95);
    verify(mockEntityManager).removeEntity(mockFood);
    verify(mockAntEntity).setCarriedEntity(null);
  }

  @Test
  public void consume_foodFromNest() {
    when(mockCell.getEntity(EntityType.NEST)).thenReturn(mockNest);
    when(mockAntEntity.getMaxEnergy()).thenReturn(100);
    when(mockAntEntity.getEnergy()).thenReturn(50);
    when(mockNest.getEnergy()).thenReturn(200);

    AntOutput output = new AntOutput();
    output.setConsume(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockAntEntity).setEnergy(100);
    verify(mockNest).setEnergy(150);
  }

  @Test
  public void consume_allFoodFromNest() {
    when(mockCell.getEntity(EntityType.NEST)).thenReturn(mockNest);
    when(mockAntEntity.getMaxEnergy()).thenReturn(100);
    when(mockAntEntity.getEnergy()).thenReturn(50);
    when(mockNest.getEnergy()).thenReturn(20);

    AntOutput output = new AntOutput();
    output.setConsume(true);
    mockOutput(output);

    activeAnt.performAction(mockEnvironment, mockEntityManager, random);

    verify(mockAntEntity).setEnergy(70);
    verify(mockNest).setEnergy(0);
  }

  @Test
  public void outputReplayData() throws IOException {
    AntOutputDataOutputStream out = new AntOutputDataOutputStream();
    List<AntOutput> expectedOutputs = new ArrayList<>();
    for (int cnt = 1; cnt <= 100; cnt++) {
      AntOutput antOutput = AntSimulationTestUtil.randomAntOutput(random);
      out.writeAntOutput(antOutput);
      expectedOutputs.add(antOutput);
      mockOutput(antOutput);
      activeAnt.performAction(mockEnvironment, mockEntityManager, random);
    }
    out.close();

    AntSimulationState state = new AntSimulationState();
    activeAnt.updateState(state);
    assertArrayEquals(out.toByteArray(),
        state.getEntityReplayDataStore(mockAntEntity).get(ActiveAnt.OUTPUT_REPLAY_DATA_KEY));

    // The output replay data is cleared when the state is set.
    activeAnt.setState(new AntSimulationState());

    out = new AntOutputDataOutputStream();
    AntOutput antOutput = AntSimulationTestUtil.randomAntOutput(random);
    out.writeAntOutput(antOutput);
    mockOutput(antOutput);
    activeAnt.performAction(mockEnvironment, mockEntityManager, random);
    out.close();

    activeAnt.updateState(state);
    assertArrayEquals(out.toByteArray(),
        state.getEntityReplayDataStore(mockAntEntity).get(ActiveAnt.OUTPUT_REPLAY_DATA_KEY));
  }

  private void mockEntityAtCellInDirection(
      EntityType entityType, EntityModel entity, Cell cell, Direction direction) {
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

  private void mockOutput(AntOutput output) {
    when(mockAntBehavior.getOutput(any(RandomGenerator.class))).thenReturn(output);
  }
}
