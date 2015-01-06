package org.mechaverse.simulation.ant.core.entity.ant;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.common.model.MoveDirection;
import org.mechaverse.simulation.common.model.TurnDirection;

/**
 * Unit test for {@link AntOutput}.
 */
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
    assertEquals(false, output.shouldPickUpOrDrop());
    assertEquals(false, output.shouldLeavePheromone());
    assertEquals(0, output.getPheromoneType());
  }

  @Test
  public void moveDirection() {
    output.setMoveDirection(MoveDirection.FORWARD);
    assertEquals(MoveDirection.FORWARD, output.getMoveDirection());

    output.setMoveDirection(MoveDirection.NONE);
    assertEquals(MoveDirection.NONE, output.getMoveDirection());
  }

  @Test
  public void turnDirection() {
    for (TurnDirection turnDirection : TurnDirection.values()) {
      output.setTurnDirection(turnDirection);
      assertEquals(turnDirection, output.getTurnDirection());
    }
  }

  @Test
  public void pickUpOrDrop() {
    output.setPickUpOrDrop(true);
    assertEquals(true, output.shouldPickUpOrDrop());

    output.setPickUpOrDrop(false);
    assertEquals(false, output.shouldPickUpOrDrop());
  }

  @Test
  public void leavePheromone() {
    for (int type = 0; type < 8; type++) {
      output.setLeavePheromone(true);
      output.setPheromoneType(type);
      assertEquals(true, output.shouldLeavePheromone());
      assertEquals(type, output.getPheromoneType());
      
      output.setLeavePheromone(false);
      output.setPheromoneType(type);
      assertEquals(false, output.shouldLeavePheromone());
      assertEquals(type, output.getPheromoneType());
    }
  }

  @Test
  public void consume() {
    output.setConsume(true);
    assertEquals(true, output.shouldConsume());

    output.setConsume(false);
    assertEquals(false, output.shouldConsume());
  }

  @Test
  public void dataSize() {
    assertEquals(AntOutput.DATA_SIZE, new AntOutput().getData().length);
  }
}
