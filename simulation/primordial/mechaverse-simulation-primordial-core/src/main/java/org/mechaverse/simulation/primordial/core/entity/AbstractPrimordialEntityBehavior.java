package org.mechaverse.simulation.primordial.core.entity;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.EntityBehavior;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.SimulationModelUtil;
import org.mechaverse.simulation.common.util.SimulationUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialCellModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public abstract class AbstractPrimordialEntityBehavior implements
    EntityBehavior<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

  private final PrimordialEntityInput input = new PrimordialEntityInput();
  protected final EntityModel entity;

  protected AbstractPrimordialEntityBehavior(
      EntityModel entity) {
    this.entity = entity;
  }

  @Override
  public void updateInput(PrimordialEnvironmentModel env, RandomGenerator random) {
    input.resetToDefault();
    input.setEnergy(entity.getEnergy(), entity.getMaxEnergy());

    PrimordialCellModel cell = env.getCell(entity);

    // Front cell sensor.
    PrimordialCellModel frontCell = env.getCellInDirection(cell, entity.getDirection());
    if (frontCell != null) {
      EntityModel frontEntity = frontCell.getEntity();
      if (frontEntity != null) {
        input.setFrontSensor(
            frontCell.getEntityType(), frontEntity.getDirection(), frontEntity.getId());
      }
    } else {
      input.setFrontSensor(null, null, "");
    }

    // EntityModel and food sensors.
    boolean nearbyEntity = false;
    boolean nearbyFood = false;
    for (Direction direction : SimulationModelUtil.DIRECTIONS) {
      if (nearbyEntity && nearbyFood) {
        break;
      }
      PrimordialCellModel neighborCell = env.getCellInDirection(cell, direction);
      if (neighborCell != null) {
        EntityType neighborEntityType = neighborCell.getEntityType();
        if (neighborEntityType == EntityType.ENTITY) {
          nearbyEntity = true;
        } else if (neighborEntityType == EntityType.FOOD) {
          nearbyFood = true;
        }
      }
    }

    input.setEntitySensor(nearbyEntity);
    input.setFoodSensor(nearbyFood);

    setInput(input, random);
  }

  @Override
  public void performAction(PrimordialEnvironmentModel env,
          EntityManager<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> entityManager,
          RandomGenerator random) {
    PrimordialEntityOutput output = getOutput(random);
    PrimordialCellModel cell = env.getCell(entity);
    PrimordialCellModel frontCell = env.getCellInDirection(cell, entity.getDirection());

    entity.setAge(entity.getAge() + 1);

    entity.setEnergy(entity.getEnergy() - 1);
    if (entity.getEnergy() <= 0) {
      onRemoveEntity();
      entityManager.removeEntity(entity);
      return;
    }

    // Consume action.
    if (output.shouldConsume()) {
      consumeFood(cell.getEntity(EntityType.FOOD), entityManager);
    }

    // Move action.
    switch (output.getMoveDirection()) {
      case NONE:
        break;
      case FORWARD:
        move(cell, frontCell, env);
        break;
      case BACKWARD:
        move(cell, env.getCellInDirection(cell,
            SimulationUtil.oppositeDirection(entity.getDirection())), env);
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
  }

  /**
   * Sets the current input.
   */
  protected abstract void setInput(PrimordialEntityInput input, RandomGenerator random);

  /**
   * Returns output values based on the current input.
   */
  protected abstract PrimordialEntityOutput getOutput(RandomGenerator random);

  private boolean move(PrimordialCellModel fromCell, PrimordialCellModel toCell, PrimordialEnvironmentModel env) {
    if (toCell != null && canMoveToCell(toCell)) {
      env.moveEntityToCell(EntityType.ENTITY, fromCell, toCell);
      return true;
    }
    return false;
  }

  private boolean canMoveToCell(PrimordialCellModel cell) {
    // An entity can move to the cell if it does not contain another entity or a barrier
    return !cell.hasEntity(EntityType.ENTITY) && !cell.hasEntity(EntityType.BARRIER);
  }

  private boolean consumeFood(EntityModel food, EntityManager entityManager) {
    if (food != null) {
      addEnergy(food.getEnergy());
      entityManager.removeEntity(food);
      return true;
    }
    return false;
  }

  private void addEnergy(int amount) {
    int energy = entity.getEnergy() + amount;
    entity.setEnergy(energy <= entity.getMaxEnergy() ? energy : entity.getMaxEnergy());
  }
}
