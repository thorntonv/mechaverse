package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link CellEnvironment}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellEnvironmentTest {

  // TODO(vthornton): Implement additional tests.

  @Mock Environment mockEnvironmentModel;

  @Test
  public void getDirection() {
    when(mockEnvironmentModel.getWidth()).thenReturn(100);
    when(mockEnvironmentModel.getHeight()).thenReturn(100);

    CellEnvironment env = new CellEnvironment(mockEnvironmentModel);
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
