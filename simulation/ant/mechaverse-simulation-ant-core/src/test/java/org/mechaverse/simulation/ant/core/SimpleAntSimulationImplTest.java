package org.mechaverse.simulation.ant.core;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-simulation-context-simple.xml")
public class SimpleAntSimulationImplTest extends AbstractAntSimulationImplTest {

  @Autowired private ObjectFactory<AntSimulationImpl> simulationImplFactory;

  @Override
  protected AntSimulationImpl newSimulationImpl() {
    return simulationImplFactory.getObject();
  }

  @Override
  protected int testIterationCount() {
    return 2500;
  }

  @Override
  protected int smallTestIterationCount() {
    return 100;
  }
}
