package org.mechaverse.simulation.common.cellautomaton.environment;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.util.TestCellEnvironmentModel;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractCellEnvironmentModelTest {

  // TODO(thorntonv): Implement additional tests.

  @Test
  public void getDirection() {
    TestCellEnvironmentModel env = new TestCellEnvironmentModel();
    env.setWidth(200);
    env.setHeight(200);
    assertEquals(Direction.NORTH, env.getDirection(env.getCell(50, 0), env.getCell(25, 0)));
    assertEquals(Direction.SOUTH, env.getDirection(env.getCell(25, 0), env.getCell(50, 0)));
    assertEquals(Direction.EAST, env.getDirection(env.getCell(0, 50), env.getCell(0, 75)));
    assertEquals(Direction.WEST, env.getDirection(env.getCell(0, 75), env.getCell(0, 50)));
    assertEquals(Direction.NORTH_EAST, env.getDirection(env.getCell(50, 50), env.getCell(0, 99)));
    assertEquals(Direction.NORTH_WEST, env.getDirection(env.getCell(50, 50), env.getCell(0, 0)));
    assertEquals(Direction.SOUTH_EAST, env.getDirection(env.getCell(50, 50), env.getCell(99, 99)));
    assertEquals(Direction.SOUTH_WEST, env.getDirection(env.getCell(50, 50), env.getCell(99, 0)));
  }
}
