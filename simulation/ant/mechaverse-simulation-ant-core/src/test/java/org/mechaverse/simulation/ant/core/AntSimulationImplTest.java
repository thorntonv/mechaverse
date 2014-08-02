package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;

import com.google.common.base.Optional;

/**
 * Unit test for {@link AntSimulationImpl}.
 */
public class AntSimulationImplTest {

  private static class RandomActiveEntityProvider extends SimpleActiveEntityProvider {

    @Override
    public Optional<ActiveEntity> getActiveEntity(Entity entity) {
      if (entity instanceof Ant) {
        Ant ant = (Ant) entity;
        return Optional.<ActiveEntity>of(new ActiveAnt(ant, new RandomAntBehavior()));
      }
      return super.getActiveEntity(entity);
    }
  }

  private RandomGenerator random;

  @Before
  public void setUp() {
    this.random = new JDKRandomGenerator();
    random.setSeed(AntSimulationImplTest.class.getName().hashCode());
  }

  @Test
  public void simulate() {
    AntSimulationImpl simulation = new AntSimulationImpl();

    for (int cnt = 0; cnt < 10000; cnt++) {
      simulation.step();
    }
  }

  @Test
  public void simulate_random() {
    AntSimulationImpl simulation = new AntSimulationImpl(new RandomActiveEntityProvider(), random);

    for (int cnt = 0; cnt < 25000; cnt++) {
      simulation.step();
    }
  }
}
