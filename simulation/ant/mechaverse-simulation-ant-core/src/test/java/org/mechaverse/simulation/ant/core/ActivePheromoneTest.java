package org.mechaverse.simulation.ant.core;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.api.model.Pheromone;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link ActivePheromone}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivePheromoneTest {

  @Mock CellEnvironment mockEnvironment;
  @Mock Pheromone mockPheromone;
  @Mock EntityManager mockEntityManager;

  private ActivePheromone activePheromone;
  private RandomGenerator random;

  @Before
  public void setUp() {
    activePheromone = new ActivePheromone(mockPheromone);
    random = new Well19937c(ActivePheromoneTest.class.getName().hashCode());
  }

  @Test
  public void update() {
    when(mockPheromone.getEnergy()).thenReturn(10);
    activePheromone.performAction(mockEnvironment, mockEntityManager, random);
    verify(mockPheromone).setEnergy(9);
  }

  @Test
  public void update_noEnergy() {
    when(mockPheromone.getEnergy()).thenReturn(1).thenReturn(0);
    activePheromone.performAction(mockEnvironment, mockEntityManager, random);
    verify(mockPheromone).setEnergy(0);
    verify(mockEntityManager).removeEntity(activePheromone);
  }
}
