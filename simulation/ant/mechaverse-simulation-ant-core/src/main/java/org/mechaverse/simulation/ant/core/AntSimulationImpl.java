package org.mechaverse.simulation.ant.core;

import static org.mechaverse.simulation.ant.core.AntSimulationUtil.canMoveAntToCell;
import static org.mechaverse.simulation.ant.core.AntSimulationUtil.turnCCW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.mechaverse.simulation.ant.api.SimulationStateUtil;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationState;
import org.mechaverse.simulation.common.cell.EnvironmentGenerator;

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
  }

  public SimulationState getState() {
    return state;
  }

  public void setState(SimulationState state) {
    this.state = state;

    environments.clear();
    for (Environment environment : SimulationStateUtil.getEnvironments(state)) {
      environments.add(new CellEnvironment(environment));
    }
  }

  public void step() {
    for (CellEnvironment env : environments) {
      for (Ant ant : env.getAnts()) {
        updateAnt(ant, env);
      }
    }
  }

  private void updateAnt(Ant ant, CellEnvironment env) {
    // TODO(thorntonv): Replace this hard coded behavior.
    if (!moveAntForward(ant, env)) {
      turnCCW(ant);
    }
  }

  private boolean moveAntForward(Ant ant, CellEnvironment env) {
    Cell cell = env.getCell(ant);
    Cell targetCell = env.getCellInDirection(cell, ant.getDirection());
    if (targetCell != null && canMoveAntToCell(targetCell)) {
      env.moveAntToCell(ant, cell, targetCell);
      return true;
    }
    return false;
  }
}
