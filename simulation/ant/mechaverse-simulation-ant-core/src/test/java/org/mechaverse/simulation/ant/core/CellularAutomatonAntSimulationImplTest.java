package org.mechaverse.simulation.ant.core;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.core.entity.ant.CellularAutomatonAntBehavior;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.spring.AntSimulationConfig;
import org.mechaverse.simulation.common.model.EntityModel;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AntSimulationConfig.class})
public class CellularAutomatonAntSimulationImplTest extends AbstractAntSimulationImplTest {

  @Autowired
  private ObjectFactory<AntSimulationImpl> simulationImplFactory;

  @Override
  protected AntSimulationImpl newSimulationImpl() {
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
    AntSimulationImpl simulation = newSimulationImpl();
    simulation.setState(simulation.generateRandomState());

    for (int cnt = 0; cnt < 25; cnt++) {
      simulation.step();
      AntSimulationModel state = simulation.getState();

      int antCount = 0;
      for (EntityModel entity : state.getEnvironment().getEntities()) {
        if (entity instanceof Ant) {
          assertTrue(entity.dataContainsKey((CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY)));
          assertTrue(entity.getData(CellularAutomatonAntBehavior.AUTOMATON_STATE_KEY).length > 0);
          assertTrue(entity.dataContainsKey(CellularAutomatonAntBehavior.AUTOMATON_OUTPUT_MAP_KEY));
          assertTrue(entity.getData(CellularAutomatonAntBehavior.AUTOMATON_OUTPUT_MAP_KEY).length > 0);
          assertTrue(entity.dataContainsKey(CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY));
          assertTrue(entity.getData(CellularAutomatonAntBehavior.AUTOMATON_BIT_OUTPUT_MAP_KEY).length > 0);
          antCount++;
        }
      }
    }
  }
}
