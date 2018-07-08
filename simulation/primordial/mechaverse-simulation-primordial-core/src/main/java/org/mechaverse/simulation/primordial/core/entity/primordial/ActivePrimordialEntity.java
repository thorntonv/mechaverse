package org.mechaverse.simulation.primordial.core.entity.primordial;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.SimulationModelUtil;
import org.mechaverse.simulation.common.util.SimulationUtil;
import org.mechaverse.simulation.primordial.core.Cell;
import org.mechaverse.simulation.primordial.core.CellEnvironment;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationState;
import org.mechaverse.simulation.primordial.core.entity.ActiveEntity;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;

/**
 * An primordial that active in the simulation. An active primordial receives sensory information about itself and
 * the environment and is able to perform actions.
 */

public final class ActivePrimordialEntity implements ActiveEntity {

  /**
   * Determines the action an primordial should take based on the input.
   */
  public interface PrimordialEntityBehavior {

    void setEntity(PrimordialEntityModel entity);

    /**
     * Sets the current input.
     */
    void setInput(PrimordialEntityInput input, RandomGenerator random);

    /**
     * Returns output values based on the current input.
     */
    PrimordialEntityOutput getOutput(RandomGenerator random);

    void onRemoveEntity();

    void setState(PrimordialSimulationState state);
    void updateState(PrimordialSimulationState state);
  }

  private final PrimordialEntityModel entity;
  private final PrimordialEntityBehavior behavior;
  private final PrimordialEntityInput input = new PrimordialEntityInput();

  public ActivePrimordialEntity(PrimordialEntityModel entity, PrimordialEntityBehavior behavior) {
    this.entity = entity;
    this.behavior = behavior;
    behavior.setEntity(entity);
  }

  @Override
  public void updateInput(CellEnvironment env, RandomGenerator random) {
    input.resetToDefault();
    input.setEnergy(entity.getEnergy(), entity.getMaxEnergy());

    Cell cell = env.getCell(entity);

    // Front cell sensor.
    Cell frontCell = env.getCellInDirection(cell, entity.getDirection());
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
      Cell neighborCell = env.getCellInDirection(cell, direction);
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

    behavior.setInput(input, random);
  }

  @Override
  public void performAction(CellEnvironment env, EntityManager entityManager,
      RandomGenerator random) {
    PrimordialEntityOutput output = behavior.getOutput(random);
    Cell cell = env.getCell(entity);
    Cell frontCell = env.getCellInDirection(cell, entity.getDirection());

    entity.setAge(entity.getAge() + 1);

    entity.setEnergy(entity.getEnergy() - 1);
    if (entity.getEnergy() <= 0) {
      behavior.onRemoveEntity();
      entityManager.removeEntity(this.getEntity());
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

  @Override
  public PrimordialEntityModel getEntity() {
    return entity;
  }

  @Override
  public EntityType getType() {
    return EntityType.ENTITY;
  }

  @Override
  public void setState(PrimordialSimulationState state) {
    behavior.setState(state);
  }

  @Override
  public void updateState(PrimordialSimulationState state) {
    behavior.updateState(state);
  }

  @Override
  public void onRemoveEntity() {
    behavior.onRemoveEntity();
  }

  private boolean move(Cell fromCell, Cell toCell, CellEnvironment env) {
    if (toCell != null && canMoveToCell(toCell)) {
      env.moveEntityToCell(EntityType.ENTITY, fromCell, toCell);
      return true;
    }
    return false;
  }

  private boolean canMoveToCell(Cell cell) {
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
