package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.util.SimulationUtil;

/**
 * Unit tests for {@link AntSimulationUtil}.
 */
public class AntSimulationUtilTest {

  @Test
  public void testTurnCW() {
    Ant ant = new Ant();
    for(Direction direction : Direction.values()) {
      ant.setDirection(direction);
      SimulationUtil.turnCW(ant);
      assertEquals(SimulationUtil.directionCW(direction), ant.getDirection());
    }
  }

  @Test
  public void testTurnCCW() {
    Ant ant = new Ant();
    for(Direction direction : Direction.values()) {
      ant.setDirection(direction);
      SimulationUtil.turnCCW(ant);
      assertEquals(SimulationUtil.directionCCW(direction), ant.getDirection());
    }
  }

  @Test
  public void directionCW() {
    assertEquals(Direction.SOUTH_EAST, SimulationUtil.directionCW(Direction.EAST));
    assertEquals(Direction.SOUTH, SimulationUtil.directionCW(Direction.SOUTH_EAST));
    assertEquals(Direction.SOUTH_WEST, SimulationUtil.directionCW(Direction.SOUTH));
    assertEquals(Direction.WEST, SimulationUtil.directionCW(Direction.SOUTH_WEST));
    assertEquals(Direction.NORTH_WEST, SimulationUtil.directionCW(Direction.WEST));
    assertEquals(Direction.NORTH, SimulationUtil.directionCW(Direction.NORTH_WEST));
    assertEquals(Direction.NORTH_EAST, SimulationUtil.directionCW(Direction.NORTH));
    assertEquals(Direction.EAST, SimulationUtil.directionCW(Direction.NORTH_EAST));
  }

  @Test
  public void testDirectionCCW() {
    assertEquals(Direction.NORTH_EAST, SimulationUtil.directionCCW(Direction.EAST));
    assertEquals(Direction.NORTH, SimulationUtil.directionCCW(Direction.NORTH_EAST));
    assertEquals(Direction.NORTH_WEST, SimulationUtil.directionCCW(Direction.NORTH));
    assertEquals(Direction.WEST, SimulationUtil.directionCCW(Direction.NORTH_WEST));
    assertEquals(Direction.SOUTH_WEST, SimulationUtil.directionCCW(Direction.WEST));
    assertEquals(Direction.SOUTH, SimulationUtil.directionCCW(Direction.SOUTH_WEST));
    assertEquals(Direction.SOUTH_EAST, SimulationUtil.directionCCW(Direction.SOUTH));
    assertEquals(Direction.EAST, SimulationUtil.directionCCW(Direction.SOUTH_EAST));
  }
}
