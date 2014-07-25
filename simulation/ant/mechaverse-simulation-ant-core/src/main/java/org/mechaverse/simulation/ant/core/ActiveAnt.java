package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Pheromone;
import org.mechaverse.simulation.ant.api.util.EntityUtil;

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
     * Sets the current input
     */
    void setInput(AntInput input);

    /**
     * Returns output values based on the current input.
     */
    AntOutput getOutput();
  }

  private static final EntityType[] CARRIABLE_ENTITY_TYPES =
      {EntityType.DIRT, EntityType.FOOD,EntityType.ROCK};

  private final Ant antEntity;
  private final AntBehavior behavior;
  private EntityType carriedEntityType = EntityType.NONE;
  private final AntInput input = new AntInput();

  public ActiveAnt(Ant entity, AntBehavior behavior) {
    this.antEntity = entity;
    this.behavior = behavior;
    if (antEntity.getCarriedEntity() != null) {
      this.carriedEntityType = EntityUtil.getType(antEntity.getCarriedEntity());
    }
  }

  @Override
  public void updateInput(CellEnvironment env, RandomGenerator random) {
    input.resetToDefault();
    input.setEnergy(antEntity.getEnergy(), antEntity.getMaxEnergy());
    input.setDirection(antEntity.getDirection());
    input.setCarriedEntityType(carriedEntityType);

    // Cell sensor.
    Cell cell = env.getCell(antEntity);
    for (EntityType cellEntityType : EntityUtil.ENTITY_TYPES) {
      Entity cellEntity = cell.getEntity(cellEntityType);
      if (cellEntity != null && cellEntity != antEntity) {
        input.setCellSensor(cellEntityType);
        break;
      }
    }

    // Front cell sensor.
    Cell frontCell = env.getCellInDirection(cell, antEntity.getDirection());
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
    Direction frontLeftDirection = AntSimulationUtil.directionCCW(antEntity.getDirection());
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
    Direction frontRightDirection = AntSimulationUtil.directionCW(antEntity.getDirection());
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
    behavior.setInput(input);
  }

  @Override
  public void performAction(
      CellEnvironment env, EntityManager entityManager, RandomGenerator random) {
    AntOutput output = behavior.getOutput();

    antEntity.setEnergy(antEntity.getEnergy() - 1);
    if (antEntity.getEnergy() <= 0) {
      entityManager.removeEntity(this);
      return;
    }

    Cell cell = env.getCell(antEntity);
    Cell frontCell = env.getCellInDirection(cell, antEntity.getDirection());

    // Consume.
    if (output.shouldConsume()) {
      if(carriedEntityType == EntityType.FOOD) {
        // Consume food that the ant is carrying.
        if(consumeFood(antEntity.getCarriedEntity(), entityManager)) {
          antEntity.setCarriedEntity(null);
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
      } else if (frontCell != null && drop(frontCell)) {
        return;
      }
    }

    // Leave pheromone action.
    int pheromoneType = output.shouldLeavePheromone();
    if (pheromoneType > 0) {
      leavePheromone(cell, pheromoneType, entityManager);
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
        AntSimulationUtil.turnCW(antEntity);
        return;
      case COUNTERCLOCKWISE:
        AntSimulationUtil.turnCCW(antEntity);
        return;
    }
  }

  @Override
  public Ant getEntity() {
    return antEntity;
  }

  @Override
  public EntityType getType() {
    return EntityType.ANT;
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
    for (EntityType type : CARRIABLE_ENTITY_TYPES) {
      Entity carriableEntity = cell.getEntity(type);
      if (carriableEntity != null) {
        cell.removeEntity(type);
        antEntity.setCarriedEntity(carriableEntity);
        this.carriedEntityType = type;
        return true;
      }
    }

    return false;
  }

  private boolean drop(Cell cell) {
    // The cell must not already have an entity of the given type.
    if (cell.getEntity(carriedEntityType) == null) {
      Entity carriedEntity = antEntity.getCarriedEntity();
      cell.setEntity(carriedEntity, carriedEntityType);
      antEntity.setCarriedEntity(null);
      carriedEntityType = EntityType.NONE;
      return true;
    }
    return false;
  }

  private void leavePheromone(Cell cell, int type, EntityManager entityManager) {
    Pheromone pheromone = new Pheromone();
    pheromone.setX(cell.getColumn());
    pheromone.setY(cell.getRow());
    pheromone.setValue(type);
    pheromone.setMaxEnergy(15);
    pheromone.setEnergy(15);
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
      int energyNeeded = antEntity.getMaxEnergy() - antEntity.getEnergy();
      int energy = energyNeeded <= nest.getEnergy() ? energyNeeded : nest.getEnergy();
      addEnergy(energy);
      nest.setEnergy(nest.getEnergy() - energy);
    }
    return false;
  }

  private void updateCarriedEntityLocation() {
    Entity carriedEntity = antEntity.getCarriedEntity();
    if (carriedEntity != null) {
      carriedEntity.setX(antEntity.getX());
      carriedEntity.setY(antEntity.getY());
    }
  }

  private void addEnergy(int amount) {
    int energy = antEntity.getEnergy() + amount;
    antEntity.setEnergy(energy <= antEntity.getMaxEnergy() ? energy : antEntity.getMaxEnergy());
  }
}
