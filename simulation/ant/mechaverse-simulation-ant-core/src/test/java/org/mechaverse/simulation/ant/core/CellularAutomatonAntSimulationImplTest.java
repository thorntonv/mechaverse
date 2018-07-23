package org.mechaverse.simulation.ant.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.mechaverse.simulation.ant.core.entity.ant.CellularAutomatonAntBehavior;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.model.EntityModel;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

public class CellularAutomatonAntSimulationImplTest extends AbstractAntSimulationImplTest {

  private List<AbstractApplicationContext> contexts = new ArrayList<>();

  @After
  public void cleanUp() {
    for (AbstractApplicationContext context : contexts) {
      context.close();
    }
    contexts.clear();
  }

  @Override
  protected AntSimulationImpl newSimulationImpl() {
    AbstractApplicationContext context =
        new ClassPathXmlApplicationContext("test-simulation-context-cellautomaton.xml");
    contexts.add(context);
    return (AntSimulationImpl) context.getBean(Simulation.class);
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
