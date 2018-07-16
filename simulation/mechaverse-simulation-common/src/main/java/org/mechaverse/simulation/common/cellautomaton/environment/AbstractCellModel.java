package org.mechaverse.simulation.common.cellautomaton.environment;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mechaverse.simulation.common.model.EntityModel;

public abstract class AbstractCellModel<
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>> {

  private final int row;
  private final int column;

  private ENT_MODEL primaryEntity;
  private ENT_TYPE primaryEntityType;

  private final Map<ENT_TYPE, ENT_MODEL> entities;

  public AbstractCellModel(int row, int column) {
    this.row = row;
    this.column = column;
    this.entities = new HashMap<>();
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

  public void setEntity(ENT_MODEL entity) {
    entity.setX(column);
    entity.setY(row);
    ENT_TYPE type = entity.getType();
    entities.put(type, entity);

    if (primaryEntityType == null || type.ordinal() <= primaryEntityType.ordinal()) {
      primaryEntity = entity;
      primaryEntityType = type;
    }
  }

  public ENT_MODEL getEntity() {
    return primaryEntity;
  }

  public ENT_TYPE getEntityType() {
    return primaryEntityType;
  }

  public boolean hasEntity(ENT_TYPE type) {
    return entities.containsKey(type);
  }

  public ENT_MODEL getEntity(ENT_TYPE type) {
    return entities.get(type);
  }

  public ENT_MODEL removeEntity(ENT_MODEL entity) {
    ENT_TYPE type = entity.getType();
    return entities.get(type) == entity ? removeEntity(type) : null;
  }


  public ENT_MODEL removeEntity(ENT_TYPE type) {
    ENT_MODEL removedEntity = entities.remove(type);
    if (primaryEntityType == type) {
      for (ENT_TYPE newType : entities.keySet()) {
        primaryEntity = entities.get(newType);
        if (primaryEntity != null) {
          primaryEntityType = newType;
          return removedEntity;
        }
      }
      primaryEntity = null;
      primaryEntityType = null;
    }
    return removedEntity;
  }

  public List<ENT_MODEL> getEntities() {
    ImmutableList.Builder<ENT_MODEL> builder = ImmutableList.builder();
    for (ENT_MODEL entity : entities.values()) {
      if (entity != null) {
        builder.add(entity);
      }
    }
    return builder.build();
  }

  public void clear() {
    primaryEntity = null;
    primaryEntityType = null;
    entities.clear();
  }
}
