package org.mechaverse.simulation.primordial.core.entity;

import org.mechaverse.simulation.common.Entity;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialCellEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;

/**
 * An primordial that active in the simulation. An active primordial receives sensory information
 * about itself and the environment and is able to perform actions.
 */

public final class ActivePrimordialEntity implements
    Entity<SimulationModel, PrimordialCellEnvironmentModel, EntityModel, EntityType> {

  private final PrimordialEntityModel entity;
  private final AbstractPrimordialEntityBehavior behavior;

  public ActivePrimordialEntity(PrimordialEntityModel entity,
      AbstractPrimordialEntityBehavior behavior) {
    this.entity = entity;
    this.behavior = behavior;
  }

  @Override
  public PrimordialEntityModel getModel() {
    return entity;
  }

  @Override
  public EntityType getType() {
    return EntityType.ENTITY;
  }

  @Override
  public AbstractPrimordialEntityBehavior getBehavior() {
    return behavior;
  }

  @Override
  public void setState(SimulationModel state) {
    behavior.setState(state);
  }

  @Override
  public void updateState(SimulationModel state) {
    behavior.updateState(state);
  }

  @Override
  public void onRemoveEntity() {
    behavior.onRemoveEntity();
  }

}
