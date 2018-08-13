package org.mechaverse.simulation.primordial;

import org.junit.runner.RunWith;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationImpl;
import org.mechaverse.simulation.primordial.core.spring.PrimordialSimulationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PrimordialSimulationConfig.class})
public class CellularAutomatonPrimordialSimulationImplTest extends AbstractPrimordialSimulationImplTest {

  @Autowired
  private ApplicationContext appContext;

  @Override
  protected PrimordialSimulationImpl newSimulationImpl() {
    return appContext.getAutowireCapableBeanFactory().getBean(PrimordialSimulationImpl.class);
  }

  @Override
  protected int testIterationCount() {
    return 1000;
  }

  @Override
  protected int smallTestIterationCount() {
    return 100;
  }
}
