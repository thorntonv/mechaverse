package org.mechaverse.simulation.primordial.core.entity;

import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;

/**
 * An primordial that active in the simulation. An active primordial receives sensory information
 * about itself and the environment and is able to perform actions.
 */

public final class PrimordialEntity extends AbstractPrimordialEntity {

  private final PrimordialEntityModel entity;
  private final AbstractPrimordialEntityBehavior behavior;

  public PrimordialEntity(PrimordialEntityModel entity,
      AbstractPrimordialEntityBehavior behavior) {
    this.entity = entity;
    this.behavior = behavior;
  }

  @Override
  public PrimordialEntityModel getModel() {
    return entity;
  }

  @Override
  public AbstractPrimordialEntityBehavior getBehavior() {
    return behavior;
  }

}
