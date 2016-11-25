package org.mechaverse.simulation.ant.core.entity;

import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.Barrier;
import org.mechaverse.simulation.ant.core.model.Conduit;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.ant.core.model.Dirt;
import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.model.Food;
import org.mechaverse.simulation.ant.core.model.Nest;
import org.mechaverse.simulation.ant.core.model.Pheromone;
import org.mechaverse.simulation.ant.core.model.Rock;

import com.google.common.collect.Ordering;

/**
 * Entity utility methods.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EntityUtil {

  public static final EntityType[] ENTITY_TYPES = EntityType.values();


  /**
   * An {@link Ordering} that uniquely orders entities. Entities are ordered first by cell with
   * cells in lower numbered rows before cells in higher and cells in lower numbered columns before
   * cells in higher. Entities in the same cell are ordered first by type based on the
   * {@link EntityType} ordinal value and then by id.
   */
  public static final Ordering<Entity> ENTITY_ORDERING = new Ordering<Entity>() {
    @Override
    public int compare(Entity left, Entity right) {
      if (left.getY() < right.getY()) {
        return -1;
      } else if (left.getY() > right.getY()) {
        return 1;
      }
      if (left.getX() < right.getX()) {
        return -1;
      } else if (left.getX() > right.getX()) {
        return 1;
      }
      EntityType leftType = EntityUtil.getType(left);
      EntityType rightType = EntityUtil.getType(right);
      if (leftType.ordinal() < rightType.ordinal()) {
        return -1;
      } else if (leftType.ordinal() > rightType.ordinal()) {
        return 1;
      }
      return left.getId().compareTo(right.getId());
    }
  };

  /**
   * Returns the {@link EntityType} of the given {@link Entity}.
   */
  public static EntityType getType(Entity entity) {
    if (entity instanceof Ant) {
      return EntityType.ANT;
    } else if (entity instanceof Barrier) {
      return EntityType.BARRIER;
    } else if (entity instanceof Conduit) {
      return EntityType.CONDUIT;
    } else if (entity instanceof Dirt) {
      return EntityType.DIRT;
    } else if (entity instanceof Food) {
      return EntityType.FOOD;
    } else if (entity instanceof Nest) {
      return EntityType.NEST;
    } else if (entity instanceof Pheromone) {
      return EntityType.PHEROMONE;
    } else if (entity instanceof Rock) {
      return EntityType.ROCK;
    }
    return null;
  }

  /**
   * Returns a new {@link Entity} instance of the given type.
   */
  public static Entity newEntity(EntityType entityType) {
    switch (entityType) {
      case NONE:
        break;
      case ANT:
        return new Ant();
      case BARRIER:
        return new Barrier();
      case CONDUIT:
        return new Conduit();
      case DIRT:
        return new Dirt();
      case FOOD:
        return new Food();
      case NEST:
        return new Nest();
      case PHEROMONE:
        return new Pheromone();
      case ROCK:
        return new Rock();
    }
    return null;
  }
}
