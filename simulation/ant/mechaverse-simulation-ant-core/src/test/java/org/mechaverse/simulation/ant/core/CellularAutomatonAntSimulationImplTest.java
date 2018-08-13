package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertTrue;
import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_BIT_OUTPUT_MAP_KEY;
import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_OUTPUT_MAP_KEY;
import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_STATE_KEY;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.spring.AntSimulationConfig;
import org.mechaverse.simulation.common.model.EntityModel;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    return 100;
  }

  @Test
  public void simulate_verifyEntityState() {
    try (AntSimulationImpl simulation = newSimulationImpl()) {
      simulation.setState(simulation.generateRandomState());

      for (int cnt = 0; cnt < 25; cnt++) {
        simulation.step();
        AntSimulationModel state = simulation.getState();

        int antCount = 0;
        for (EntityModel entity : state.getEnvironment().getEntities()) {
          if (entity instanceof Ant) {
            assertTrue(entity.dataContainsKey((CELLULAR_AUTOMATON_STATE_KEY)));
            assertTrue(entity.getData(CELLULAR_AUTOMATON_STATE_KEY).length > 0);
            assertTrue(entity.dataContainsKey(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY));
            assertTrue(entity.getData(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY).length > 0);
            assertTrue(entity.dataContainsKey(CELLULAR_AUTOMATON_BIT_OUTPUT_MAP_KEY));
            assertTrue(entity.getData(CELLULAR_AUTOMATON_BIT_OUTPUT_MAP_KEY).length > 0);
            antCount++;
          }
        }
      }
    }
  }
}
