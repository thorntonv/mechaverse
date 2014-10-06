package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.core.entity.ant.CircuitAntBehavior;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CircuitAntSimulationImplTest extends AbstractAntSimulationImplTest {

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
        new ClassPathXmlApplicationContext("simulation-context-circuit.xml");
    contexts.add(context);
    return (AntSimulationImpl) context.getBean(Simulation.class);
  }

  @Override
  protected int testIterationCount() {
    return 100;
  }

  @Test
  public void simulate_verifyEntityState() throws IOException {
    AntSimulationImpl simulation = newSimulationImpl();
    simulation.setState(simulation.generateRandomState());

    for (int cnt = 0; cnt < 25; cnt++) {
      simulation.step();
      AntSimulationState state = simulation.getState();

      int antCount = 0;
      for (Entity entity : state.getModel().getEnvironment().getEntities()) {
        if (entity instanceof Ant) {
          SimulationDataStore entityDataStore = state.getEntityValues(entity);
          assertTrue(entityDataStore.containsKey(CircuitAntBehavior.CIRCUIT_STATE_KEY));
          assertTrue(entityDataStore.get(CircuitAntBehavior.CIRCUIT_STATE_KEY).length > 0);
          assertTrue(entityDataStore.containsKey(CircuitAntBehavior.CIRCUIT_OUTPUT_MAP_KEY));
          assertTrue(entityDataStore.get(CircuitAntBehavior.CIRCUIT_OUTPUT_MAP_KEY).length > 0);
          assertTrue(entityDataStore.containsKey(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY));
          assertTrue(entityDataStore.get(CircuitAntBehavior.CIRCUIT_BIT_OUTPUT_MAP_KEY).length > 0);
          antCount++;
        }
      }
      // State should have model and config values.
      // Each ant should have genetic data, circuit state, output map, and bit output map values.
      assertEquals("Key set: " + state.keySet(), antCount * 4 + 2, state.keySet().size());
    }
  }
}
