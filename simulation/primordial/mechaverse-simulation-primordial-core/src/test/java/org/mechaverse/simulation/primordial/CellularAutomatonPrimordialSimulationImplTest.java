package org.mechaverse.simulation.primordial;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationImpl;
import org.mechaverse.simulation.primordial.core.spring.PrimordialSimulationConfig;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PrimordialSimulationConfig.class})
public class CellularAutomatonPrimordialSimulationImplTest extends AbstractPrimordialSimulationImplTest {

  @Autowired
  private ObjectFactory<PrimordialSimulationImpl> simulationImplFactory;

  @Override
  protected PrimordialSimulationImpl newSimulationImpl() {
    return simulationImplFactory.getObject();
  }

  @Override
  protected int testIterationCount() {
    return 1000;
  }

  @Override
  protected int smallTestIterationCount() {
    return 50;
  }

  @Test
  public void simulate_verifyEntityState() throws IOException {
  }
}
