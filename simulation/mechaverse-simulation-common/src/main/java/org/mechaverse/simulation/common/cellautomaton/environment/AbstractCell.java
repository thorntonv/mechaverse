package org.mechaverse.simulation.common.cellautomaton.environment;

import java.util.List;

import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.model.EntityModel;
import com.google.common.collect.ImmutableList;

public abstract class AbstractCell<T extends Enum<T>> {

  private final int row;
  private final int column;

  private EntityModel primaryEntity;
  private T primaryEntityType;
  private final EntityFactory<T> entityFactory;

  private final EntityModel[] entities;

  public AbstractCell(int row, int column, EntityFactory<T> entityFactory) {
    this.row = row;
    this.column = column;
    this.entityFactory = entityFactory;
    this.entities = new EntityModel[entityFactory.getTypeValues().length];
  }

  public int getRow() {
    return row;
  }

  public int getColumn() {
    return column;
  }

  public boolean isEmpty() {
    return primaryEntity == null;
  }

  public void setEntity(EntityModel entity) {
    setEntity(entity, entityFactory.getType(entity));
  }

  public void setEntity(EntityModel entity, T type) {
    entity.setX(column);
    entity.setY(row);
    entities[type.ordinal()] = entity;

    if (primaryEntityType == null || type.ordinal() <= primaryEntityType.ordinal()) {
      primaryEntity = entity;
      primaryEntityType = type;
    }
  }

  public EntityModel getEntity() {
    return primaryEntity;
  }

  public T getEntityType() {
    return primaryEntityType;
  }

  public boolean hasEntity(T type) {
    return entities[type.ordinal()] != null;
  }

  public EntityModel getEntity(T type) {
    return entities[type.ordinal()];
  }

  public EntityModel removeEntity(EntityModel entity) {
    T type = entityFactory.getType(entity);
    return entities[type.ordinal()] == entity ? removeEntity(type) : null;
  }

  public EntityModel removeEntity(T type) {
    EntityModel removedEntity = entities[type.ordinal()];
    entities[type.ordinal()] = null;
    if (primaryEntityType == type) {
      for (int idx = type.ordinal() + 1; idx < entities.length; idx++) {
        primaryEntity = entities[idx];
        if (primaryEntity != null) {
          primaryEntityType = entityFactory.getTypeValues()[idx];
          return removedEntity;
        }
      }
      primaryEntity = null;
      primaryEntityType = null;
    }
    return removedEntity;
  }

  public List<EntityModel> getEntities() {
    ImmutableList.Builder<EntityModel> builder = ImmutableList.builder();
    for (EntityModel entity : entities) {
      if (entity != null) {
        builder.add(entity);
      }
    }
    return builder.build();
  }

  public void clear() {
    primaryEntity = null;
    primaryEntityType = null;
    for (int idx = 0; idx < entities.length; idx++) {
      entities[idx] = null;
    }
  }
}
