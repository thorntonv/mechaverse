package org.mechaverse.simulation.ant.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.mechaverse.simulation.ant.api.SimulationStateUtil;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationState;
import org.mechaverse.simulation.common.cellautomata.EnvironmentGenerator;

public final class AntSimulationImpl {

  private static final int DEFAULT_ENVIRONMENT_WIDTH = 200;
  private static final int DEFAULT_ENVIRONMENT_HEIGHT = 200;

  private SimulationState state;
  private final List<CellEnvironment> environments = new ArrayList<CellEnvironment>();
  private final EnvironmentGenerator<CellEnvironment, EntityType> environmentGenerator =
      new AntSimulationEnvironmentGenerator();

  public AntSimulationImpl() {
    state = new SimulationState();
    state.setEnvironment(environmentGenerator.generate(
        DEFAULT_ENVIRONMENT_WIDTH, DEFAULT_ENVIRONMENT_HEIGHT, new Random()).getEnvironment());
    setState(state);
  }

  public SimulationState getState() {
    return state;
  }

  public void setState(SimulationState state) {
    this.state = state;

    environments.clear();
    for (Environment environment : SimulationStateUtil.getEnvironments(state)) {
      CellEnvironment cells = new CellEnvironment(environment);
      environments.add(cells);
      for (Entity entity : environment.getEntities()) {
        if (entity instanceof Ant) {
          cells.addActiveEntity(new ActiveAnt((Ant) entity, new SimpleAntBehavior()));
        }
      }
    }
  }

  public void step() {
    for (CellEnvironment env : environments) {
      for (ActiveEntity activeEntity : env.getActiveEntities()) {
        activeEntity.update(env);
      }
    }
  }
}
