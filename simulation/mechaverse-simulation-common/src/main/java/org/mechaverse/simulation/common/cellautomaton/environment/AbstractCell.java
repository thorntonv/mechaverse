package org.mechaverse.simulation.common.cellautomaton.environment;

import java.util.List;

import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.model.Entity;
import com.google.common.collect.ImmutableList;

public abstract class AbstractCell<T extends Enum<T>> {

  private final int row;
  private final int column;

  private Entity primaryEntity;
  private T primaryEntityType;
  private final EntityFactory<T> entityFactory;

  private final Entity[] entities;

  public AbstractCell(int row, int column, EntityFactory<T> entityFactory) {
    this.row = row;
    this.column = column;
    this.entityFactory = entityFactory;
    this.entities = new Entity[entityFactory.getTypeValues().length];
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

  public void setEntity(Entity entity) {
    setEntity(entity, entityFactory.getType(entity));
  }

  public void setEntity(Entity entity, T type) {
    entity.setX(column);
    entity.setY(row);
    entities[type.ordinal()] = entity;

    if (primaryEntityType == null || type.ordinal() <= primaryEntityType.ordinal()) {
      primaryEntity = entity;
      primaryEntityType = type;
    }
  }

  public Entity getEntity() {
    return primaryEntity;
  }

  public T getEntityType() {
    return primaryEntityType;
  }

  public boolean hasEntity(T type) {
    return entities[type.ordinal()] != null;
  }

  public Entity getEntity(T type) {
    return entities[type.ordinal()];
  }

  public Entity removeEntity(Entity entity) {
    T type = entityFactory.getType(entity);
    return entities[type.ordinal()] == entity ? removeEntity(type) : null;
  }

  public Entity removeEntity(T type) {
    Entity removedEntity = entities[type.ordinal()];
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

  public List<Entity> getEntities() {
    ImmutableList.Builder<Entity> builder = ImmutableList.builder();
    for (Entity entity : entities) {
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
