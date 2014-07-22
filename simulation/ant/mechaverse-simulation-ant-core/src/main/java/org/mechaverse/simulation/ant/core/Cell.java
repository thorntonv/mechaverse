package org.mechaverse.simulation.ant.core;

import java.util.List;

import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.util.EntityUtil;

import com.google.common.collect.ImmutableList;

public final class Cell {

  private final int row;
  private final int column;

  private Entity primaryEntity;
  private EntityType primaryEntityType;

  private Entity[] entities = new Entity[EntityType.values().length];

  public Cell(int row, int column) {
    this.row = row;
    this.column = column;
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
    setEntity(entity, EntityUtil.getType(entity));
  }

  public void setEntity(Entity entity, EntityType type) {
    entities[type.ordinal()] = entity;

    if (primaryEntityType == null || type.ordinal() <= primaryEntityType.ordinal()) {
      primaryEntity = entity;
      primaryEntityType = type;
    }
  }

  public Entity getEntity() {
    return primaryEntity;
  }

  public EntityType getEntityType() {
    return primaryEntityType;
  }

  public boolean hasEntity(EntityType type) {
    return entities[type.ordinal()] != null;
  }

  public Entity getEntity(EntityType type) {
    return entities[type.ordinal()];
  }

  public Entity removeEntity(EntityType type) {
    Entity removedEntity = entities[type.ordinal()];
    entities[type.ordinal()] = null;
    if (primaryEntityType == type) {
      for (int idx = type.ordinal() + 1; idx < entities.length; idx++) {
        primaryEntity = entities[idx];
        if (primaryEntity != null) {
          primaryEntityType = EntityUtil.ENTITY_TYPES[idx];
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
}
