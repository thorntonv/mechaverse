package org.mechaverse.simulation.ant.core.entity.ant;

import org.mechaverse.simulation.ant.core.model.Ant;

/**
 * An ant that active in the simulation. An active ant receives sensory information about itself and
 * the environment and is able to perform actions.
 */

public final class AntEntity extends AbstractAntEntity {

  private AbstractAntBehavior behavior;

  public AntEntity() {}

  public AntEntity(AbstractAntBehavior behavior) {
    this.behavior = behavior;
  }

  @Override
  public Ant getModel() {
    return behavior.getModel();
  }

  @Override
  public AbstractAntBehavior getBehavior() {
    return behavior;
  }
}
