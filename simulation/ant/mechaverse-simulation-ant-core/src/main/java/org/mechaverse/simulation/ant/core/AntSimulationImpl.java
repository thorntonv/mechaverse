package org.mechaverse.simulation.ant.core;

import static org.mechaverse.simulation.ant.core.AntSimulationUtil.canMoveAntToCell;
import static org.mechaverse.simulation.ant.core.AntSimulationUtil.turnCCW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.mechaverse.simulation.ant.api.SimulationStateUtil;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationState;

public final class AntSimulationImpl {

  private SimulationState state;
  private final List<CellEnvironment> environments = new ArrayList<CellEnvironment>();

  public AntSimulationImpl() {
    state = new SimulationState();
    Environment env = new Environment();
    env.setId(UUID.randomUUID().toString());
    env.setWidth(10);
    env.setHeight(10);
    state.setEnvironment(env);
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
      moveAntToCell(ant, cell, targetCell);
      return true;
    }
    return false;
  }

  private void moveAntToCell(Ant ant, Cell fromCell, Cell targetCell) {
    fromCell.setAnt(null);
    ant.setX(targetCell.getColumn());
    ant.setY(targetCell.getRow());
    targetCell.setAnt(ant);
  }
}
