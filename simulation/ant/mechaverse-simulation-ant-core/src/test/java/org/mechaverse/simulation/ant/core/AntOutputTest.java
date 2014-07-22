package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.core.AntOutput.MoveDirection;
import org.mechaverse.simulation.ant.core.AntOutput.TurnDirection;

public class AntOutputTest {

  private Random random;
  private AntOutput output;

  @Before
  public void setUp() {
    this.random = new Random(AntOutputTest.class.getName().hashCode());

    int[] data = new int[AntOutput.DATA_SIZE];
    for (int idx = 0; idx < data.length; idx++) {
      data[idx] = random.nextInt();
    }
    this.output = new AntOutput(data);
  }

  @Test
  public void initialState() {
    AntOutput output = new AntOutput();

    assertEquals(MoveDirection.NONE, output.getMoveDirection());
    assertEquals(TurnDirection.NONE, output.getTurnDirection());
    assertEquals(false, output.pickUp());
    assertEquals(false, output.drop());
    assertEquals(0, output.getLeavePheromone());
  }

  @Test
  public void moveDirection() {
    for (MoveDirection moveDirection : MoveDirection.values()) {
      output.setMoveDirection(moveDirection);
      assertEquals(moveDirection, output.getMoveDirection());
    }
  }

  @Test
  public void turnDirection() {
    for (TurnDirection turnDirection : TurnDirection.values()) {
      output.setTurnDirection(turnDirection);
      assertEquals(turnDirection, output.getTurnDirection());
    }
  }

  @Test
  public void pickUp() {
    output.setPickUp(true);
    assertEquals(true, output.pickUp());

    output.setPickUp(false);
    assertEquals(false, output.pickUp());
  }

  @Test
  public void drop() {
    output.setDrop(true);
    assertEquals(true, output.drop());

    output.setDrop(false);
    assertEquals(false, output.drop());
  }

  @Test
  public void leavePheromone() {
    for (int type = 0; type < 15; type++) {
      output.setLeavePheromone(type);
      assertEquals(type, output.getLeavePheromone());
    }
  }

  @Test
  public void dataSize() {
    assertEquals(AntOutput.DATA_SIZE, new AntOutput().getData().length);
  }
}
