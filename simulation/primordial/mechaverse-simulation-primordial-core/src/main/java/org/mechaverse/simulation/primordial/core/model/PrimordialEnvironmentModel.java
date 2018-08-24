package org.mechaverse.simulation.primordial.core.model;

import java.util.ArrayList;
import java.util.List;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;

public final class PrimordialEnvironmentModel extends
    EnvironmentModel<EntityModel<EntityType>, EntityType> {

  private int[][] entityMatrix;
  private PrimordialEntityModel[][] entityModelMatrix;
  private int[][] foodMatrix;

  public int[][] getEntityMatrix() {
    return entityMatrix;
  }

  public int[][] getFoodMatrix() {
    return foodMatrix;
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
    entityMatrix[row][col] = 1;
  }

  @Override
  public void remove(EntityModel<EntityType> entity) {
    super.remove(entity);
    if (entity.getType() == EntityType.ENTITY) {
      int row = entity.getY() + 1;
      int col = entity.getX() + 1;
      entityModelMatrix[row][col] = null;
      entityMatrix[row][col] = 0;
    }
  }

  public PrimordialEntityModel getEntity(int row, int col) {
    return entityModelMatrix[row + 1][col + 1];
  }

  public void addFood(int row, int col) {
    foodMatrix[row+1][col+1] = 1;
  }

  public boolean hasFood(int row, int col) {
    return foodMatrix[row + 1][col + 1] > 0;
  }

  public void removeFood(int row, int col) {
    foodMatrix[row+1][col+1] = 0;
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
    for(int row = 0; row < foodMatrix.length; row++) {
      for(int col = 0; col < foodMatrix[row].length; col++) {
        if(foodMatrix[row][col] > 0) {
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
    entityMatrix = new int[getHeight()+2][];
    entityModelMatrix = new PrimordialEntityModel[getHeight()+2][];
    foodMatrix = new int[getHeight()+2][];
    // Allocate cells.
    for (int row = 0; row < entityMatrix.length; row++) {
      entityMatrix[row] = new int[getWidth()+2];
      entityModelMatrix[row] = new PrimordialEntityModel[getWidth()+2];
      foodMatrix[row] = new int[getWidth()+2];
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
