package org.mechaverse.simulation.ant.core.entity.ant;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * An ant that active in the simulation. An active ant receives sensory information about itself and
 * the environment and is able to perform actions.
 */
public final class ActiveAnt implements ActiveEntity {

  public static final String OUTPUT_REPLAY_DATA_KEY = "outputReplayData";

  /**
   * Determines the action an ant should take based on the input.
   */
  public static interface AntBehavior {

    void setEntity(Ant entity);

    /**
     * Sets the current input.
     */
    void setInput(AntInput input, RandomGenerator random);

    /**
     * Returns output values based on the current input.
     */
    AntOutput getOutput(RandomGenerator random);

    void onRemoveEntity();

    void setState(AntSimulationState state);
    void updateState(AntSimulationState state);
  }

  private static final EntityType[] CARRIABLE_ENTITY_TYPES =
      {EntityType.DIRT, EntityType.FOOD,EntityType.ROCK};

  private static final Logger logger = LoggerFactory.getLogger(ActiveAnt.class);

  @Value("#{properties['pheromoneInitialEnergy']}") private int pheromoneInitialEnergy;


  private final Ant entity;
  private final AntBehavior behavior;
  private EntityType carriedEntityType = EntityType.NONE;
  private final AntInput input = new AntInput();
  private AntOutputDataOutputStream outputReplayDataOutputStream = new AntOutputDataOutputStream();

  public ActiveAnt(Ant entity, AntBehavior behavior) {
    this.entity = entity;
    this.behavior = behavior;
    behavior.setEntity(entity);
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
  public void performAction(CellEnvironment env, EntityManager entityManager,
      RandomGenerator random) {
    AntOutput output = behavior.getOutput(random);

    try {
      outputReplayDataOutputStream.writeAntOutput(output);
      logger.trace("Recorded output {} for ant {}", Arrays.toString(output.getData()), entity.getId());
    } catch (IOException e) {}

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
        }
      } else if (consumeFood(cell.getEntity(EntityType.FOOD), entityManager)) {
      } else if (consumeFoodFromNest(cell.getEntity(EntityType.NEST))) {}
    }

    // Pickup / Drop action.
    if (output.shouldPickUpOrDrop()) {
      if (carriedEntityType == EntityType.NONE) {
        if (pickup(cell)) {} else if (pickup(frontCell)) {}
      } else {
        if (carriedEntityType == EntityType.FOOD && drop(cell)) {} else if (drop(frontCell)) {}
      }
    }

    // Leave pheromone action.
    if (output.shouldLeavePheromone()) {
      leavePheromone(cell, output.getPheromoneType(), entityManager);
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
            AntSimulationUtil.oppositeDirection(entity.getDirection())), env);
        break;
    }

    // Turn action.
    switch (output.getTurnDirection()) {
      case NONE:
        break;
      case CLOCKWISE:
        AntSimulationUtil.turnCW(entity);
        break;
      case COUNTERCLOCKWISE:
        AntSimulationUtil.turnCCW(entity);
        break;
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
  public void setState(AntSimulationState state) {
    behavior.setState(state);
    // Output replay data will contain all outputs since the state was last set.
    outputReplayDataOutputStream = new AntOutputDataOutputStream();
  }

  @Override
  public void updateState(AntSimulationState state) {
    behavior.updateState(state);
    try {
      outputReplayDataOutputStream.close();
      state.getEntityReplayDataStore(entity).put(
        OUTPUT_REPLAY_DATA_KEY, outputReplayDataOutputStream.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onRemoveEntity() {
    behavior.onRemoveEntity();
  }

  private boolean move(Cell fromCell, Cell toCell, CellEnvironment env) {
    if (toCell != null && canMoveToCell(toCell)) {
      env.moveEntityToCell(EntityType.ANT, fromCell, toCell);
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

  private void leavePheromone(Cell cell, int type, EntityManager entityManager) {
    Pheromone pheromone = new Pheromone();
    pheromone.setValue(type);
    pheromone.setEnergy(pheromoneInitialEnergy);
    pheromone.setMaxEnergy(pheromoneInitialEnergy);

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
