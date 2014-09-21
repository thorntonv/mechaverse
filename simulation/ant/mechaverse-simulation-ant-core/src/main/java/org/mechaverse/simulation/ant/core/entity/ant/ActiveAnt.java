package org.mechaverse.simulation.ant.core.entity.ant;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationConfig;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Pheromone;
import org.mechaverse.simulation.ant.api.util.EntityUtil;
import org.mechaverse.simulation.ant.core.ActiveEntity;
import org.mechaverse.simulation.ant.core.AntSimulationUtil;
import org.mechaverse.simulation.ant.core.Cell;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;
import org.mechaverse.simulation.common.SimulationDataStore;

/**
 * An ant that active in the simulation. An active ant receives sensory information about itself and
 * the environment and is able to perform actions.
 */
public final class ActiveAnt implements ActiveEntity {

  /**
   * Determines the action an ant should take based on the input.
   */
  public static interface AntBehavior {

    /**
     * Sets the current input.
     */
    void setInput(AntInput input, RandomGenerator random);

    /**
     * Returns output values based on the current input.
     */
    AntOutput getOutput(RandomGenerator random);

    void onRemoveEntity();

    void setState(SimulationDataStore state);
    SimulationDataStore getState();
  }

  private static final EntityType[] CARRIABLE_ENTITY_TYPES =
      {EntityType.DIRT, EntityType.FOOD,EntityType.ROCK};

  private final Ant entity;
  private final AntBehavior behavior;
  private EntityType carriedEntityType = EntityType.NONE;
  private final AntInput input = new AntInput();

  public ActiveAnt(Ant entity, AntBehavior behavior) {
    this.entity = entity;
    this.behavior = behavior;
    if (entity.getCarriedEntity() != null) {
      this.carriedEntityType = EntityUtil.getType(entity.getCarriedEntity());
    }
  }

  @Override
  public void updateInput(CellEnvironment env, RandomGenerator random) {
    input.resetToDefault();
    input.setEnergy(entity.getEnergy(), entity.getMaxEnergy());
    input.setDirection(entity.getDirection());
    input.setCarriedEntityType(carriedEntityType);

    // Cell sensor.
    Cell cell = env.getCell(entity);
    for (EntityType cellEntityType : EntityUtil.ENTITY_TYPES) {
      Entity cellEntity = cell.getEntity(cellEntityType);
      if (cellEntity != null && cellEntity != entity) {
        input.setCellSensor(cellEntityType);
        break;
      }
    }

    // Nest direction sensor.
    input.setNestDirection(env.getNestDirection(cell));

    // Front cell sensor.
    Cell frontCell = env.getCellInDirection(cell, entity.getDirection());
    if (frontCell != null) {
      Entity frontEntity = frontCell.getEntity();
      if (frontEntity != null) {
        input.setFrontSensor(
            frontCell.getEntityType(), frontEntity.getDirection(), frontEntity.getId());
      }
    } else {
      input.setFrontSensor(null, null, "");
    }

    // Front left cell sensor.
    Direction frontLeftDirection = AntSimulationUtil.directionCCW(entity.getDirection());
    Cell frontLeftCell = env.getCellInDirection(cell, frontLeftDirection);
    if (frontLeftCell != null) {
      Entity frontLeftEntity = frontLeftCell.getEntity();
      if (frontLeftEntity != null) {
        input.setFrontLeftSensor(frontLeftCell.getEntityType(), frontLeftEntity.getDirection());
      }
    } else {
      input.setFrontLeftSensor(null, null);
    }

    // Left cell sensor.
    Cell leftCell =
        env.getCellInDirection(cell, AntSimulationUtil.directionCCW(frontLeftDirection));
    if (leftCell != null) {
      Entity leftEntity = leftCell.getEntity();
      if (leftEntity != null) {
        input.setLeftSensor(leftCell.getEntityType(), leftEntity.getDirection());
      }
    } else {
      input.setLeftSensor(null, null);
    }

    // Front right cell sensor.
    Direction frontRightDirection = AntSimulationUtil.directionCW(entity.getDirection());
    Cell frontRightCell = env.getCellInDirection(cell, frontRightDirection);
    if (frontRightCell != null) {
      Entity frontRightEntity = frontRightCell.getEntity();
      if (frontRightEntity != null) {
        input.setFrontRightSensor(frontRightCell.getEntityType(), frontRightEntity.getDirection());
      }
    } else {
      input.setFrontRightSensor(null, null);
    }

    // Right cell sensor.
    Cell rightCell =
        env.getCellInDirection(cell, AntSimulationUtil.directionCW(frontRightDirection));
    if (rightCell != null) {
      Entity rightEntity = rightCell.getEntity();
      if (rightEntity != null) {
        input.setRightSensor(rightCell.getEntityType(), rightEntity.getDirection());
      }
    } else {
      input.setRightSensor(null, null);
    }

    // Pheromone sensor.
    Entity pheromoneEntity = cell.getEntity(EntityType.PHEROMONE);
    if (pheromoneEntity != null && pheromoneEntity instanceof Pheromone) {
      Pheromone pheromone = (Pheromone) pheromoneEntity;
      input.setPheromoneType(pheromone.getValue());
    }
    behavior.setInput(input, random);
  }

  @Override
  public void performAction(CellEnvironment env, AntSimulationConfig config,
      EntityManager entityManager, RandomGenerator random) {
    AntOutput output = behavior.getOutput(random);

    Cell cell = env.getCell(entity);
    Cell frontCell = env.getCellInDirection(cell, entity.getDirection());

    entity.setAge(entity.getAge() + 1);

    entity.setEnergy(entity.getEnergy() - 1);
    if (entity.getEnergy() <= 0) {
      behavior.onRemoveEntity();
      entityManager.removeEntity(this);

      // Attempt to drop the carried entity.
      if (entity.getCarriedEntity() != null) {
        if (!drop(cell)) {
          if (!drop(frontCell)) {
            // Unable to drop the carried entity. Remove the entity occupying the cell and drop.
            entityManager.removeEntity(entity.getCarriedEntity());
          }
        }
      }
      return;
    }

    // Consume action.
    if (output.shouldConsume()) {
      if(carriedEntityType == EntityType.FOOD) {
        // Consume food that the ant is carrying.
        if(consumeFood(entity.getCarriedEntity(), entityManager)) {
          entity.setCarriedEntity(null);
          carriedEntityType = EntityType.NONE;
          return;
        }
      } else if (consumeFood(cell.getEntity(EntityType.FOOD), entityManager)) {
        return;
      } else if (consumeFoodFromNest(cell.getEntity(EntityType.NEST))) {
        return;
      }
    }

    // Pickup / Drop action.
    if (carriedEntityType == EntityType.NONE && output.shouldPickUp()) {
      if (pickup(cell)) {
        return;
      } else if (pickup(frontCell)) {
        return;
      }
    } else if (carriedEntityType != EntityType.NONE && output.shouldDrop()) {
      if (carriedEntityType == EntityType.FOOD && drop(cell)) {
        return;
      } else if (drop(frontCell)) {
        return;
      }
    }

    // Leave pheromone action.
    int pheromoneType = output.shouldLeavePheromone();
    if (pheromoneType > 0) {
      leavePheromone(cell, pheromoneType, config, entityManager);
      return;
    }

    // Move action.
    switch (output.getMoveDirection()) {
      case NONE:
        break;
      case FORWARD:
        if (moveForward(cell, frontCell, env)) {
          return;
        }
        break;
      case BACKWARD:
        break;
    }

    // Turn action.
    switch (output.getTurnDirection()) {
      case NONE:
        break;
      case CLOCKWISE:
        AntSimulationUtil.turnCW(entity);
        return;
      case COUNTERCLOCKWISE:
        AntSimulationUtil.turnCCW(entity);
        return;
    }
  }

  @Override
  public Ant getEntity() {
    return entity;
  }

  @Override
  public EntityType getType() {
    return EntityType.ANT;
  }

  @Override
  public void setState(SimulationDataStore state) {
    behavior.setState(state);
  }

  @Override
  public SimulationDataStore getState() {
    return behavior.getState();
  }

  private boolean moveForward(Cell cell, Cell frontCell, CellEnvironment env) {
    if (frontCell != null && canMoveToCell(frontCell)) {
      env.moveEntityToCell(EntityType.ANT, cell, frontCell);
      updateCarriedEntityLocation();
      return true;
    }
    return false;
  }

  private boolean canMoveToCell(Cell cell) {
    // An an can move to the cell if it does not contain another ant, a barrier, a rock, or dirt.
    return !cell.hasEntity(EntityType.ANT) && !cell.hasEntity(EntityType.BARRIER)
        && !cell.hasEntity(EntityType.DIRT) && !cell.hasEntity(EntityType.ROCK);
  }

  private boolean pickup(Cell cell) {
    if(cell != null) {
      for (EntityType type : CARRIABLE_ENTITY_TYPES) {
        Entity carriableEntity = cell.getEntity(type);
        if (carriableEntity != null) {
          cell.removeEntity(type);
          entity.setCarriedEntity(carriableEntity);
          this.carriedEntityType = type;
          carriableEntity.setX(entity.getX());
          carriableEntity.setY(entity.getY());
          return true;
        }
      }
    }
    return false;
  }

  private boolean drop(Cell cell) {
    // The cell must not already have an entity of the given type.
    if (cell != null && cell.getEntity(carriedEntityType) == null) {
      Entity carriedEntity = entity.getCarriedEntity();
      cell.setEntity(carriedEntity, carriedEntityType);
      entity.setCarriedEntity(null);
      carriedEntityType = EntityType.NONE;
      return true;
    }
    return false;
  }

  private void leavePheromone(Cell cell, int type, AntSimulationConfig config,
      EntityManager entityManager) {
    Pheromone pheromone = new Pheromone();
    pheromone.setValue(type);
    pheromone.setMaxEnergy(config.getPheromoneInitialEnergy());
    pheromone.setEnergy(config.getPheromoneInitialEnergy());

    Entity existingPheromone = cell.getEntity(EntityType.PHEROMONE);
    if (existingPheromone != null) {
      entityManager.removeEntity(existingPheromone);
    }
    cell.setEntity(pheromone, EntityType.PHEROMONE);
    entityManager.addEntity(pheromone);
  }

  private boolean consumeFood(Entity food, EntityManager entityManager) {
    if (food != null) {
      addEnergy(food.getEnergy());
      entityManager.removeEntity(food);
      return true;
    }
    return false;
  }

  private boolean consumeFoodFromNest(Entity nest) {
    if(nest != null) {
      int energyNeeded = entity.getMaxEnergy() - entity.getEnergy();
      int energy = energyNeeded <= nest.getEnergy() ? energyNeeded : nest.getEnergy();
      addEnergy(energy);
      nest.setEnergy(nest.getEnergy() - energy);
    }
    return false;
  }

  private boolean feed(Entity entityToFeed, EntityManager entityManager, CellEnvironment env) {
    if (carriedEntityType == EntityType.FOOD) {
      Entity food = entity.getCarriedEntity();

      if (food != null) {
        int energy = entityToFeed.getEnergy() + food.getEnergy();
        entityToFeed.setEnergy(energy <= entityToFeed.getMaxEnergy()
            ? energy : entityToFeed.getMaxEnergy());
        carriedEntityType = EntityType.NONE;
        entity.setCarriedEntity(null);
        entityManager.removeEntity(food);
        return true;
      }
    }
    return false;
  }

  private void updateCarriedEntityLocation() {
    Entity carriedEntity = entity.getCarriedEntity();
    if (carriedEntity != null) {
      carriedEntity.setX(entity.getX());
      carriedEntity.setY(entity.getY());
    }
  }

  private void addEnergy(int amount) {
    int energy = entity.getEnergy() + amount;
    entity.setEnergy(energy <= entity.getMaxEnergy() ? energy : entity.getMaxEnergy());
  }
}
