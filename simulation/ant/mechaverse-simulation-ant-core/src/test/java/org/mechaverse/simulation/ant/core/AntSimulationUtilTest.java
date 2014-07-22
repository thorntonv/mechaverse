package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Direction;

/**
 * Unit tests for {@link AntSimulationUtil}.
 */
public class AntSimulationUtilTest {

  @Test
  public void testTurnCW() {
    Ant ant = new Ant();
    for(Direction direction : Direction.values()) {
      ant.setDirection(direction);
      AntSimulationUtil.turnCW(ant);
      assertEquals(AntSimulationUtil.directionCW(direction), ant.getDirection());
    }
  }

  @Test
  public void testTurnCCW() {
    Ant ant = new Ant();
    for(Direction direction : Direction.values()) {
      ant.setDirection(direction);
      AntSimulationUtil.turnCCW(ant);
      assertEquals(AntSimulationUtil.directionCCW(direction), ant.getDirection());
    }
  }

  @Test
  public void directionCW() {
    assertEquals(Direction.SOUTH_EAST, AntSimulationUtil.directionCW(Direction.EAST));
    assertEquals(Direction.SOUTH, AntSimulationUtil.directionCW(Direction.SOUTH_EAST));
    assertEquals(Direction.SOUTH_WEST, AntSimulationUtil.directionCW(Direction.SOUTH));
    assertEquals(Direction.WEST, AntSimulationUtil.directionCW(Direction.SOUTH_WEST));
    assertEquals(Direction.NORTH_WEST, AntSimulationUtil.directionCW(Direction.WEST));
    assertEquals(Direction.NORTH, AntSimulationUtil.directionCW(Direction.NORTH_WEST));
    assertEquals(Direction.NORTH_EAST, AntSimulationUtil.directionCW(Direction.NORTH));
    assertEquals(Direction.EAST, AntSimulationUtil.directionCW(Direction.NORTH_EAST));
  }

  @Test
  public void testDirectionCCW() {
    assertEquals(Direction.NORTH_EAST, AntSimulationUtil.directionCCW(Direction.EAST));
    assertEquals(Direction.NORTH, AntSimulationUtil.directionCCW(Direction.NORTH_EAST));
    assertEquals(Direction.NORTH_WEST, AntSimulationUtil.directionCCW(Direction.NORTH));
    assertEquals(Direction.WEST, AntSimulationUtil.directionCCW(Direction.NORTH_WEST));
    assertEquals(Direction.SOUTH_WEST, AntSimulationUtil.directionCCW(Direction.WEST));
    assertEquals(Direction.SOUTH, AntSimulationUtil.directionCCW(Direction.SOUTH_WEST));
    assertEquals(Direction.SOUTH_EAST, AntSimulationUtil.directionCCW(Direction.SOUTH));
    assertEquals(Direction.EAST, AntSimulationUtil.directionCCW(Direction.SOUTH_EAST));
  }
}
