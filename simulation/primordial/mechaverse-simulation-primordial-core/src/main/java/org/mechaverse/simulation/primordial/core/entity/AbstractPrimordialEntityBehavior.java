package org.mechaverse.simulation.primordial.core.entity;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.EntityBehavior;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractPrimordialEntityBehavior implements
    EntityBehavior<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

  protected final EntityModel<EntityType> entity;


  protected AbstractPrimordialEntityBehavior(EntityModel<EntityType> entity) {
    this.entity = entity;
  }

  @Override
  public final void updateInput(PrimordialEnvironmentModel env, RandomGenerator random) {
  }

  @Override
  public final void performAction(Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> env,
          RandomGenerator random) {
  }

  /**
   * Sets the current input.
   */
  protected abstract void setInput(PrimordialEntityInput input, RandomGenerator random);

  /**
   * Returns output values based on the current input.
   */
  protected abstract PrimordialEntityOutput getOutput(RandomGenerator random);
}
