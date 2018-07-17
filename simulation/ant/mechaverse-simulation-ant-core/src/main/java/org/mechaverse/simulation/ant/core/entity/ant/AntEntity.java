package org.mechaverse.simulation.ant.core.entity.ant;

import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;

/**
 * An ant that active in the simulation. An active ant receives sensory information about itself and
 * the environment and is able to perform actions.
 */

public final class AntEntity extends AbstractAntEntity {

  private AbstractAntBehavior behavior;

  public AntEntity() {}

  @Override
  public Ant getModel() {
    return behavior.getModel();
  }

  @Override
  public AbstractAntBehavior getBehavior() {
    return behavior;
  }

  @Override
  public void setState(AntSimulationModel state) {
    behavior.setState(state);
  }

  @Override
  public void updateState(AntSimulationModel state) {
    behavior.updateState(state);
  }

  @Override
  public void onRemoveEntity() {
    behavior.onRemoveEntity();
  }
}
