package org.mechaverse.simulation.primordial.core.entity;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;
import org.mechaverse.simulation.primordial.core.model.Barrier;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.Food;

import com.google.common.collect.Ordering;

/**
 * EntityModel utility methods.
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
  public static final Ordering<EntityModel> ENTITY_ORDERING = new Ordering<EntityModel>() {
    @Override
    public int compare(EntityModel left, EntityModel right) {
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
   * Returns the {@link EntityType} of the given {@link EntityModel}.
   */
  public static EntityType getType(EntityModel entity) {
    if (entity instanceof PrimordialEntityModel) {
      return EntityType.ENTITY;
    } else if (entity instanceof Barrier) {
      return EntityType.BARRIER;
    } else if (entity instanceof Food) {
      return EntityType.FOOD;
    }
    return null;
  }

  /**
   * Returns a new {@link EntityModel} instance of the given type.
   */
  public static EntityModel<EntityType> newEntity(EntityType entityType) {
    switch (entityType) {
      case NONE:
        break;
      case ENTITY:
        return new PrimordialEntityModel();
      case BARRIER:
        return new Barrier();
      case FOOD:
        return new Food();
    }
    return null;
  }
}
