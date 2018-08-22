package org.mechaverse.simulation.primordial.core.entity;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.EntityBehavior;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.SimulationUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.Food;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractPrimordialEntityBehavior implements
    EntityBehavior<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

  private final PrimordialEntityInput input = new PrimordialEntityInput();
  protected final EntityModel<EntityType> entity;

  protected AbstractPrimordialEntityBehavior(EntityModel<EntityType> entity) {
    this.entity = entity;
  }

  @Override
  public void updateInput(PrimordialEnvironmentModel env, RandomGenerator random) {
    input.resetToDefault();
    input.setEnergy(entity.getEnergy(), entity.getMaxEnergy());

    int entityRow = entity.getY();
    int entityCol = entity.getX();

    input.setFrontEntityType(env.getCellTypeInDirection(entityRow, entityCol, entity.getDirection()));
    input.setEntitySensor(env.isEntityNearby(entityRow, entityCol));
    input.setFoodSensor(env.isFoodNearby(entityRow, entityCol));

    setInput(input, random);
  }

  @Override
  public void performAction(Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> env,
          RandomGenerator random) {
    final PrimordialEnvironmentModel envModel = env.getModel();
    PrimordialEntityOutput output = getOutput(random);

    int entityRow = entity.getY();
    int entityCol = entity.getX();
    int entityEnergy = entity.getEnergy() - 1;

    entity.setAge(entity.getAge() + 1);

    if (entityEnergy <= 0) {
      onRemoveEntity();
      env.removeEntity(entity);
      return;
    }

    // Consume action.
    if (output.shouldConsume() && envModel.hasFood(entityRow, entityCol)) {
      env.getModel().removeFood(entityRow, entityCol);
      env.removeEntity(new Food());
      entityEnergy += 100;
    }

    // Move action.
    switch (output.getMoveDirection()) {
      case NONE:
        break;
      case FORWARD:
        envModel.moveEntityToCellInDirection(entityRow, entityCol, entity);
        break;
      case BACKWARD:
        break;
    }

    // Turn action.
    switch (output.getTurnDirection()) {
      case NONE:
        break;
      case CLOCKWISE:
        SimulationUtil.turnCW(entity);
        break;
      case COUNTERCLOCKWISE:
        SimulationUtil.turnCCW(entity);
        break;
    }

    entity.setEnergy(entityEnergy);
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
