package org.mechaverse.simulation.ant.core.entity.ant;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.entity.EntityUtil;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.Cell;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.model.Pheromone;
import org.mechaverse.simulation.common.EntityBehavior;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.util.SimulationUtil;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractAntBehavior implements EntityBehavior<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

  private static final EntityType[] CARRIABLE_ENTITY_TYPES =
      {EntityType.DIRT, EntityType.FOOD,EntityType.ROCK};

  protected Ant entity;
  private final AntInput input = new AntInput();

  @Value("#{properties['pheromoneInitialEnergy']}") private int pheromoneInitialEnergy;
  private int leavePheromoneEnergyCost = 2;
  private int pickUpEnergyCost = 1;

  public Ant getModel() {
    return entity;
  }

  void setModel(Ant entity) {
    this.entity = entity;
  }

  @Override
  public void updateInput(CellEnvironment env, RandomGenerator random) {
    input.resetToDefault();
    input.setEnergy(entity.getEnergy(), entity.getMaxEnergy());
    input.setDirection(entity.getDirection());
    input.setCarriedEntityType(getCarriedEntityType());

    // Cell sensor.
    Cell cell = env.getCell(entity);
    for (EntityType cellEntityType : EntityUtil.ENTITY_TYPES) {
      EntityModel cellEntity = cell.getEntity(cellEntityType);
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
      EntityModel frontEntity = frontCell.getEntity();
      if (frontEntity != null) {
        input.setFrontSensor(
            frontCell.getEntityType(), frontEntity.getDirection(), frontEntity.getId());
      }
    } else {
      input.setFrontSensor(null, null, "");
    }

    // Front left cell sensor.
    Direction frontLeftDirection = SimulationUtil.directionCCW(entity.getDirection());
    Cell frontLeftCell = env.getCellInDirection(cell, frontLeftDirection);
    if (frontLeftCell != null) {
      EntityModel frontLeftEntity = frontLeftCell.getEntity();
      if (frontLeftEntity != null) {
        input.setFrontLeftSensor(frontLeftCell.getEntityType(), frontLeftEntity.getDirection());
      }
    } else {
      input.setFrontLeftSensor(null, null);
    }

    // Left cell sensor.
    Cell leftCell =
        env.getCellInDirection(cell, SimulationUtil.directionCCW(frontLeftDirection));
    if (leftCell != null) {
      EntityModel leftEntity = leftCell.getEntity();
      if (leftEntity != null) {
        input.setLeftSensor(leftCell.getEntityType(), leftEntity.getDirection());
      }
    } else {
      input.setLeftSensor(null, null);
    }

    // Front right cell sensor.
    Direction frontRightDirection = SimulationUtil.directionCW(entity.getDirection());
    Cell frontRightCell = env.getCellInDirection(cell, frontRightDirection);
    if (frontRightCell != null) {
      EntityModel frontRightEntity = frontRightCell.getEntity();
      if (frontRightEntity != null) {
        input.setFrontRightSensor(frontRightCell.getEntityType(), frontRightEntity.getDirection());
      }
    } else {
      input.setFrontRightSensor(null, null);
    }

    // Right cell sensor.
    Cell rightCell =
        env.getCellInDirection(cell, SimulationUtil.directionCW(frontRightDirection));
    if (rightCell != null) {
      EntityModel rightEntity = rightCell.getEntity();
      if (rightEntity != null) {
        input.setRightSensor(rightCell.getEntityType(), rightEntity.getDirection());
      }
    } else {
      input.setRightSensor(null, null);
    }

    // Pheromone sensor.
    EntityModel pheromoneEntity = cell.getEntity(EntityType.PHEROMONE);
    if (pheromoneEntity instanceof Pheromone) {
      Pheromone pheromone = (Pheromone) pheromoneEntity;
      input.setPheromoneType(pheromone.getValue());
    }
    setInput(input, random);
  }

  @Override
  public void performAction(Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> env,
      RandomGenerator random) {
    AntOutput output = getOutput(random);
    CellEnvironment envModel = env.getModel();
    Cell cell = envModel.getCell(entity);
    Cell frontCell = envModel.getCellInDirection(cell, entity.getDirection());

    entity.setAge(entity.getAge() + 1);

    entity.setEnergy(entity.getEnergy() - 1);
    if (entity.getEnergy() <= 0) {
      onRemoveEntity();
      env.removeEntity(this.getModel());

      // Attempt to drop the carried entity.
      if (entity.getCarriedEntity() != null) {
        if (!drop(cell)) {
          if (!drop(frontCell)) {
            // Unable to drop the carried entity. Remove the entity occupying the cell and drop.

            env.removeEntity(entity.getCarriedEntity());
          }
        }
      }
      return;
    }

    // Consume action.
    if (output.shouldConsume()) {
      if(getCarriedEntityType() == EntityType.FOOD) {
        // Consume food that the ant is carrying.
        if(consumeFood(entity.getCarriedEntity(), env)) {
          entity.setCarriedEntity(null);
        }
      } else if (consumeFood(cell.getEntity(EntityType.FOOD), env)) {
      } else if (consumeFoodFromNest(cell.getEntity(EntityType.NEST))) {}
    }

    // Pickup / Drop action.
    if (output.shouldPickUpOrDrop()) {
      if (getCarriedEntityType() == EntityType.NONE) {
        if (pickup(cell)) {} else if (pickup(frontCell)) {}
      } else {
        if (getCarriedEntityType() == EntityType.FOOD && drop(cell)) {} else if (drop(frontCell)) {}
      }
    }

    // Leave pheromone action.
    if (output.shouldLeavePheromone()) {
      leavePheromone(cell, output.getPheromoneType(), env);
    }

    // Move action.
    switch (output.getMoveDirection()) {
      case NONE:
        break;
      case FORWARD:
        move(cell, frontCell, envModel);
        break;
      case BACKWARD:
        move(cell, envModel.getCellInDirection(cell,
            SimulationUtil.oppositeDirection(entity.getDirection())), envModel);
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
  protected abstract void setInput(AntInput input, RandomGenerator random);

  /**
   * Returns output values based on the current input.
   */
  protected abstract AntOutput getOutput(RandomGenerator random);

  @Override
  public void onRemoveEntity() {}

  @Override
  public void setState(AntSimulationModel state) {}

  @Override
  public void updateState(AntSimulationModel state) {}

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
        EntityModel<EntityType> carriableEntity = cell.getEntity(type);
        if (carriableEntity != null) {
          cell.removeEntity(type);
          entity.setCarriedEntity(carriableEntity);
          carriableEntity.setX(entity.getX());
          carriableEntity.setY(entity.getY());

          int energy = entity.getEnergy();
          if (energy > pickUpEnergyCost) {
            entity.setEnergy(energy - pickUpEnergyCost);
          }
          return true;
        }
      }
    }
    return false;
  }

  private boolean drop(Cell cell) {
    // The cell must not already have an entity of the given type.
    if (cell != null && cell.getEntity(getCarriedEntityType()) == null) {
      EntityModel<EntityType> carriedEntity = entity.getCarriedEntity();
      cell.setEntity(carriedEntity);
      entity.setCarriedEntity(null);
      return true;
    }
    return false;
  }

  private void leavePheromone(Cell cell, int type,
      Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> env) {
    int energy = entity.getEnergy();
    if (energy > leavePheromoneEnergyCost) {
      entity.setEnergy(energy - leavePheromoneEnergyCost);

      Pheromone pheromone = new Pheromone();
      pheromone.setValue(type);
      pheromone.setEnergy(pheromoneInitialEnergy);
      pheromone.setMaxEnergy(pheromoneInitialEnergy);

      EntityModel<EntityType> existingPheromone = cell.getEntity(EntityType.PHEROMONE);
      if (existingPheromone != null) {
        env.removeEntity(existingPheromone);
      }
      cell.setEntity(pheromone);
      env.addEntity(pheromone);
    }
  }

  private boolean consumeFood(EntityModel<EntityType> food,
      Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> env) {
    if (food != null) {
      addEnergy(food.getEnergy());
      env.removeEntity(food);
      return true;
    }
    return false;
  }

  private boolean consumeFoodFromNest(EntityModel nest) {
    if(nest != null) {
      int energyNeeded = entity.getMaxEnergy() - entity.getEnergy();
      int energy = energyNeeded <= nest.getEnergy() ? energyNeeded : nest.getEnergy();
      addEnergy(energy);
      nest.setEnergy(nest.getEnergy() - energy);
    }
    return false;
  }

  private void updateCarriedEntityLocation() {
    EntityModel carriedEntity = entity.getCarriedEntity();
    if (carriedEntity != null) {
      carriedEntity.setX(entity.getX());
      carriedEntity.setY(entity.getY());
    }
  }

  private void addEnergy(int amount) {
    int energy = entity.getEnergy() + amount;
    entity.setEnergy(energy <= entity.getMaxEnergy() ? energy : entity.getMaxEnergy());
  }

  private EntityType getCarriedEntityType() {
    return entity.getCarriedEntity() != null ? entity.getCarriedEntity().getType() : EntityType.NONE;
  }
}
