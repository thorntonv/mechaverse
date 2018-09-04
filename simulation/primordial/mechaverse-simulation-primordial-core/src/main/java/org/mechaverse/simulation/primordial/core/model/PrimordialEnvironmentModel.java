package org.mechaverse.simulation.primordial.core.model;

import java.util.ArrayList;
import java.util.List;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;

public final class PrimordialEnvironmentModel extends
    EnvironmentModel<EntityModel<EntityType>, EntityType> {

  public static final int ENTITY_MASK = 0b1;
  public static final int FOOD_ENTITY_MASK = 0b10;

  private byte[][] entityMatrix;
  private PrimordialEntityModel[][] entityModelMatrix;

  public byte[][] getEntityMatrix() {
    return entityMatrix;
  }

  public PrimordialEntityModel[][] getEntityModelMatrix() {
    return entityModelMatrix;
  }

  public boolean isValidCell(int row, int col) {
    return row >= 0 && col >= 0 && row < getHeight() && col < getWidth();
  }

  public void addEntity(PrimordialEntityModel entityModel) {
    int row = entityModel.getY() + 1;
    int col = entityModel.getX() + 1;
    entityModelMatrix[row][col] = entityModel;
    entityMatrix[row][col] |= ENTITY_MASK;
  }

  @Override
  public void remove(EntityModel<EntityType> entity) {
    super.remove(entity);
    if (entity.getType() == EntityType.ENTITY && entity.getX() >= 0 && entity.getY() >= 0) {
      int row = entity.getY() + 1;
      int col = entity.getX() + 1;
      entityModelMatrix[row][col] = null;
      entityMatrix[row][col] &= ~ENTITY_MASK;
    }
  }

  public PrimordialEntityModel getEntity(int row, int col) {
    return entityModelMatrix[row + 1][col + 1];
  }

  public void addFood(int row, int col) {
    entityMatrix[row+1][col+1] |= FOOD_ENTITY_MASK;
  }

  public boolean hasFood(int row, int col) {
    return (entityMatrix[row + 1][col + 1] & FOOD_ENTITY_MASK) > 0;
  }

  public void removeFood(int row, int col) {
    entityMatrix[row+1][col+1] &= ~FOOD_ENTITY_MASK;
  }

  public List<EntityModel<EntityType>> getEntities() {
    if(entityModelMatrix == null) {
      initCells();
    }
    List<EntityModel<EntityType>> entities = new ArrayList<>();
    for (PrimordialEntityModel[] row : entityModelMatrix) {
      for(PrimordialEntityModel entityModel : row) {
        if(entityModel != null) {
          entities.add(entityModel);
        }
      }
    }
    for(int row = 0; row < entityMatrix.length; row++) {
      for(int col = 0; col < entityMatrix[row].length; col++) {
        if((entityMatrix[row][col] & FOOD_ENTITY_MASK) > 0) {
          Food foodModel = new Food();
          foodModel.setEnergy(100);
          foodModel.setY(row);
          foodModel.setX(col);
          entities.add(foodModel);
        }
      }
    }
    return entities;
  }

  @Override
  public EntityType[] getEntityTypes() {
    return EntityUtil.ENTITY_TYPES;
  }

  public void initCells() {
    entityMatrix = new byte[getHeight()+2][];
    entityModelMatrix = new PrimordialEntityModel[getHeight()+2][];

    // Allocate cells.
    for (int row = 0; row < entityMatrix.length; row++) {
      entityMatrix[row] = new byte[getWidth()+2];
      entityModelMatrix[row] = new PrimordialEntityModel[getWidth()+2];
    }

    // Add entities to the appropriate cells.
    for (EntityModel<EntityType> entityModel : super.getEntities()) {
      if (entityModel instanceof PrimordialEntityModel) {
        addEntity((PrimordialEntityModel) entityModel);
      } else if (entityModel.getType() == EntityType.FOOD) {
        addFood(entityModel.getY(), entityModel.getX());
      }
    }
  }
}
