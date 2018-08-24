package org.mechaverse.simulation.ant.core.entity.ant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.core.entity.ant.AntInput.SensorInfo;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.Cell;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.model.Food;
import org.mechaverse.simulation.ant.core.model.Nest;
import org.mechaverse.simulation.ant.core.model.Pheromone;
import org.mechaverse.simulation.ant.core.model.Rock;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.MoveDirection;
import org.mechaverse.simulation.common.model.TurnDirection;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link AntEntity}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractAntBehaviorTest {

  private static class TestAntBehavior extends AbstractAntBehavior {

    private AntInput input;
    private AntOutput output;

    TestAntBehavior(Ant entity) {
      super(entity);
    }

    @Override
    protected void setInput(final AntInput input, final RandomGenerator random) {
      this.input = input;
    }

    @Override
    protected AntOutput getOutput(final RandomGenerator random) {
      return output;
    }
  }

  @Mock
  private Ant mockAntEntity;
  @Mock
  private CellEnvironment mockEnvModel;
  @Mock
  private Cell mockCell;
  @Mock
  private Cell mockFrontCell;
  @Mock
  private Cell mockOtherCell;
  @Mock
  private Ant mockOtherAnt;
  @Mock
  private Food mockFood;
  @Mock
  private Nest mockNest;
  @Mock
  private Pheromone mockPheromone;
  @Mock
  private Rock mockRock;
  @Mock
  private Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> mockEnv;

  private TestAntBehavior antBehavior;

  private RandomGenerator random;

  @Before
  public void setUp() {
    antBehavior = Mockito.spy(new TestAntBehavior(mockAntEntity));

    random = RandomUtil.newGenerator(AbstractAntBehaviorTest.class.getName().hashCode());

    when(mockAntEntity.getId()).thenReturn("001");
    when(mockAntEntity.getEnergy()).thenReturn(10);
    when(mockAntEntity.getDirection()).thenReturn(Direction.EAST);
    when(mockEnv.getModel()).thenReturn(mockEnvModel);
    when(mockEnvModel.getCell(mockAntEntity)).thenReturn(mockCell);
    when(mockEnvModel.getCellInDirection(mockCell, Direction.EAST)).thenReturn(mockFrontCell);
    when(mockEnvModel.getNestDirection(any(Cell.class))).thenReturn(Direction.EAST);

    when(mockFood.getType()).thenReturn(EntityType.FOOD);
  }

  @Test
  public void age() {
    when(mockAntEntity.getCreatedIteration()).thenReturn(100L);
    mockOutput(new AntOutput());
    antBehavior.performAction(mockEnv, random);
    verify(mockAntEntity).setCreatedIteration(101L);
  }

  @Test
  public void energyLevel() {
    when(mockAntEntity.getEnergy()).thenReturn(75);
    when(mockAntEntity.getMaxEnergy()).thenReturn(100);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(5, antBehavior.input.getEnergyLevel());
  }

  @Test
  public void performAction_energyConsumed() {
    mockOutput(new AntOutput());
    antBehavior.performAction(mockEnv, random);
    verify(mockAntEntity).setEnergy(9);
  }

  @Test
  public void performAction_noEnergy() {
    mockOutput(new AntOutput());
    when(mockAntEntity.getEnergy()).thenReturn(1).thenReturn(0);
    antBehavior.performAction(mockEnv, random);
    verify(mockAntEntity).setEnergy(0);
    verify(mockEnv).removeEntity(mockAntEntity);
    verify(antBehavior).onRemoveEntity();
  }

  @Test
  public void performAction_noEnergy_carriedEntity() {
    mockOutput(new AntOutput());
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockRock);
    when(mockAntEntity.getEnergy()).thenReturn(1).thenReturn(0);
    when(mockEnvModel.getCell(mockAntEntity)).thenReturn(mockCell);

    antBehavior.performAction(mockEnv, random);
    verify(mockAntEntity).setEnergy(0);
    verify(mockEnv).removeEntity(mockAntEntity);
    verify(antBehavior).onRemoveEntity();
    verify(mockCell).setEntity(mockRock);
  }

  @Test
  public void direction() {
    when(mockAntEntity.getDirection()).thenReturn(Direction.SOUTH_WEST);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(Direction.SOUTH_WEST, antBehavior.input.getDirection());
  }

  @Test
  public void cellSensor_empty() {
    when(mockCell.getEntity(EntityType.ANT)).thenReturn(mockAntEntity);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(EntityType.NONE, antBehavior.input.getCellSensor());
  }

  @Test
  public void cellSensor_food() {
    when(mockCell.getEntity(EntityType.FOOD)).thenReturn(mockFood);
    when(mockCell.getEntity(EntityType.ANT)).thenReturn(mockAntEntity);

    antBehavior.updateInput(mockEnvModel, random);

    assertEquals(EntityType.FOOD, antBehavior.input.getCellSensor());
  }

  @Test
  public void frontCellSensor_empty() {
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(EntityType.NONE, antBehavior.input.getFrontSensor().getEntityType());
  }

  @Test
  public void frontCellSensor_noCell() {
    when(mockEnvModel.getCellInDirection(mockCell, Direction.EAST)).thenReturn(null);
    antBehavior.updateInput(mockEnvModel, random);
    assertNull(antBehavior.input.getFrontSensor().getEntityType());
  }

  @Test
  public void frontCellSensor_ant() {
    when(mockFrontCell.getEntity()).thenReturn(mockOtherAnt);
    when(mockFrontCell.getEntityType()).thenReturn(EntityType.ANT);
    when(mockOtherAnt.getDirection()).thenReturn(Direction.WEST);
    when(mockOtherAnt.getId()).thenReturn("test");

    antBehavior.updateInput(mockEnvModel, random);

    verifySensorInfo(antBehavior.input.getFrontSensor(),
        EntityType.ANT, Direction.WEST, "test".hashCode());
  }

  @Test
  public void frontCellSensor_food() {
    when(mockFrontCell.getEntity()).thenReturn(mockFood);
    when(mockFrontCell.getEntityType()).thenReturn(EntityType.FOOD);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(EntityType.FOOD, antBehavior.input.getFrontSensor().getEntityType());
  }

  @Test
  public void frontLeftCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.NORTH_EAST);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(EntityType.FOOD, antBehavior.input.getFrontLeftSensor().getEntityType());
  }

  @Test
  public void frontLeftCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.NORTH_EAST);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(EntityType.NONE, antBehavior.input.getFrontLeftSensor().getEntityType());
  }

  @Test
  public void frontLeftCellSensor_noCell() {
    antBehavior.updateInput(mockEnvModel, random);
    assertNull(antBehavior.input.getFrontLeftSensor().getEntityType());
  }

  @Test
  public void frontRightCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.SOUTH_EAST);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(EntityType.FOOD, antBehavior.input.getFrontRightSensor().getEntityType());
  }

  @Test
  public void frontRightCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.SOUTH_EAST);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(EntityType.NONE, antBehavior.input.getFrontRightSensor().getEntityType());
  }

  @Test
  public void frontRightCellSensor_noCell() {
    antBehavior.updateInput(mockEnvModel, random);
    assertNull(antBehavior.input.getFrontRightSensor().getEntityType());
  }

  @Test
  public void leftCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.NORTH);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(EntityType.FOOD, antBehavior.input.getLeftSensor().getEntityType());
  }

  @Test
  public void leftCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.NORTH);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(EntityType.NONE, antBehavior.input.getLeftSensor().getEntityType());
  }

  @Test
  public void leftCellSensor_noCell() {
    antBehavior.updateInput(mockEnvModel, random);
    assertNull(antBehavior.input.getLeftSensor().getEntityType());
  }

  @Test
  public void rightCellSensor_food() {
    mockEntityAtCellInDirection(EntityType.FOOD, mockFood, mockCell, Direction.SOUTH);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(EntityType.FOOD, antBehavior.input.getRightSensor().getEntityType());
  }

  @Test
  public void rightCellSensor_empty() {
    mockEntityAtCellInDirection(null, null, mockCell, Direction.SOUTH);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(EntityType.NONE, antBehavior.input.getRightSensor().getEntityType());
  }

  @Test
  public void rightCellSensor_noCell() {
    antBehavior.updateInput(mockEnvModel, random);
    assertNull(antBehavior.input.getRightSensor().getEntityType());
  }

  @Test
  public void pheromoneSensor_noPheromone() {
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(0, antBehavior.input.getPheromoneType());
  }

  @Test
  public void pheromoneSensor() {
    when(mockCell.getEntity(EntityType.PHEROMONE)).thenReturn(mockPheromone);
    when(mockPheromone.getValue()).thenReturn(3);
    antBehavior.updateInput(mockEnvModel, random);
    assertEquals(3, antBehavior.input.getPheromoneType());
  }

  @Test
  public void moveForward_emptyCell() {
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    mockOutput(output);
    antBehavior.performAction(mockEnv, random);
    verify(mockEnvModel).moveEntityToCell(EntityType.ANT, mockCell, mockFrontCell);
  }

  @Test
  public void moveForward_nonExistentCell() {
    when(mockEnvModel.getCellInDirection(mockCell, Direction.EAST)).thenReturn(null);
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockEnvModel, never()).moveEntityToCell(
        any(EntityType.class), any(Cell.class), any(Cell.class));
  }

  @Test
  public void moveForward_rockCell() {
    when(mockFrontCell.hasEntity(EntityType.ROCK)).thenReturn(true);
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockEnvModel, never()).moveEntityToCell(
        any(EntityType.class), any(Cell.class), any(Cell.class));
  }

  @Test
  public void moveForward_barrierCell() {
    when(mockFrontCell.hasEntity(EntityType.BARRIER)).thenReturn(true);
    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockEnvModel, never()).moveEntityToCell(
        any(EntityType.class), any(Cell.class), any(Cell.class));
  }

  @Test
  public void moveForward_carrying() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockFood);
    when(mockAntEntity.getX()).thenReturn(123);
    when(mockAntEntity.getY()).thenReturn(321);

    AntOutput output = new AntOutput();
    output.setMoveDirection(MoveDirection.FORWARD);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockFood).setX(123);
    verify(mockFood).setY(321);
    verify(mockEnvModel).moveEntityToCell(EntityType.ANT, mockCell, mockFrontCell);
  }

  @Test
  public void turnDirection_clockwise() {
    AntOutput output = new AntOutput();
    output.setTurnDirection(TurnDirection.CLOCKWISE);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockAntEntity, times(1)).setDirection(any(Direction.class));
    verify(mockAntEntity).setDirection(Direction.SOUTH_EAST);
  }

  @Test
  public void turnDirection_counterClockwise() {
    AntOutput output = new AntOutput();
    output.setTurnDirection(TurnDirection.COUNTERCLOCKWISE);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockAntEntity).setDirection(any(Direction.class));
    verify(mockAntEntity).setDirection(Direction.NORTH_EAST);
  }

  @Test
  public void turnDirection_none() {
    AntOutput output = new AntOutput();
    output.setTurnDirection(TurnDirection.NONE);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockAntEntity, never()).setDirection(any(Direction.class));
  }

  @Test
  public void pickup_food() {
    when(mockCell.getEntity(EntityType.FOOD)).thenReturn(mockFood);
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

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

    antBehavior.performAction(mockEnv, random);

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

    antBehavior.performAction(mockEnv, random);

    verify(mockCell, never()).removeEntity(any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(anyObject());
  }

  @Test
  public void pickup_pheromone() {
    when(mockCell.getEntity(EntityType.PHEROMONE)).thenReturn(mockPheromone);
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockCell, never()).removeEntity(any(EntityType.class));
    verify(mockAntEntity, never()).setCarriedEntity(anyObject());
  }

  @Test
  public void drop_notCarrying() {
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockCell, never()).setEntity(anyObject());
    verify(mockAntEntity, never()).setCarriedEntity(anyObject());
  }

  @Test
  public void drop_food() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockFood);
    
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    antBehavior.updateInput(mockEnvModel, random);
    antBehavior.performAction(mockEnv, random);

    verify(mockCell).setEntity(mockFood);
    verify(mockAntEntity).setCarriedEntity(null);
  }

  @Test
  public void drop_rock() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockRock);
    
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockFrontCell).setEntity(mockRock);
    verify(mockAntEntity).setCarriedEntity(null);
  }

  @Test
  public void drop_onOccupiedCell() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockFood);
    when(mockCell.getEntity(EntityType.FOOD)).thenReturn(mockFood);
    when(mockFrontCell.getEntity(EntityType.FOOD)).thenReturn(mockFood);
    
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockCell, never()).setEntity(anyObject());
    verify(mockAntEntity, never()).setCarriedEntity(anyObject());
  }

  @Test
  public void drop_atEnvironmentBorder() {
    when(mockEnvModel.getCellInDirection(mockCell, Direction.EAST)).thenReturn(null);
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockRock);
    AntOutput output = new AntOutput();
    output.setPickUpOrDrop(true);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockCell, never()).setEntity(anyObject());
    verify(mockAntEntity, never()).setCarriedEntity(anyObject());
  }

  @Test
  public void leavePheromone() {
    when(mockCell.getColumn()).thenReturn(123);
    when(mockCell.getRow()).thenReturn(321);
    AntOutput output = new AntOutput();
    output.setLeavePheromone(true);
    output.setPheromoneType(4);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    ArgumentCaptor<Pheromone> pheromoneCaptor = ArgumentCaptor.forClass(Pheromone.class);
    verify(mockCell).setEntity(pheromoneCaptor.capture());
    verify(mockEnv).addEntity(pheromoneCaptor.getValue());
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

    antBehavior.performAction(mockEnv, random);

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

    antBehavior.performAction(mockEnv, random);

    verify(mockAntEntity).setEnergy(90);
    verify(mockEnv).removeEntity(mockFood);
  }

  @Test
  public void consume_carriedFood() {
    when(mockAntEntity.getCarriedEntity()).thenReturn(mockFood);
    when(mockAntEntity.getMaxEnergy()).thenReturn(100);
    when(mockAntEntity.getEnergy()).thenReturn(50);
    when(mockFood.getEnergy()).thenReturn(45);

    AntOutput output = new AntOutput();
    output.setConsume(true);
    mockOutput(output);

    antBehavior.performAction(mockEnv, random);

    verify(mockAntEntity).setEnergy(95);
    verify(mockEnv).removeEntity(mockFood);
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

    antBehavior.performAction(mockEnv, random);

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

    antBehavior.performAction(mockEnv, random);

    verify(mockAntEntity).setEnergy(70);
    verify(mockNest).setEnergy(0);
  }

  private void mockEntityAtCellInDirection(
      EntityType entityType, EntityModel<EntityType> entity, Cell cell, Direction direction) {
    when(mockEnvModel.getCellInDirection(cell, direction)).thenReturn(mockOtherCell);
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
    antBehavior.output = output;
  }
}
