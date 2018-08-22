package org.mechaverse.simulation.primordial.core.model;

import java.util.ArrayList;
import java.util.List;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;
import org.mechaverse.simulation.primordial.core.entity.PrimordialEntityInput;

public final class PrimordialEnvironmentModel extends
    EnvironmentModel<EntityModel<EntityType>, EntityType> {

  private int[][] entityMatrix;
  private PrimordialEntityModel[][] entityModelMatrix;
  private int[][] foodMatrix;
  private PrimordialEntityInput[][] inputMatrix;

  private static final int[] CELL_DIRECTION_ROW_OFFSETS = new int[]{0, -1, -1, -1, 0, 1, 1, 1};
  private static final int[] CELL_DIRECTION_COL_OFFSETS = new int[]{1,  1,  0, -1, -1, -1, 0, 1};
  private static final int ENTITY_ORDINAL = EntityType.ENTITY.ordinal();
  private static final int FOOD_ORDINAL = EntityType.FOOD.ordinal();
  private static final int NONE_ORDINAL = EntityType.NONE.ordinal();

  public EntityModel<EntityType> getPrimaryEntityModel(int row, int col) {
    return entityModelMatrix[row+1][col+1];
  }

  public void updateInputMatrix() {
    for (int row = 0; row < inputMatrix.length; row++) {
      for (int col = 0; col < inputMatrix[row].length; col++) {
        int entityRow = row + 1;
        int entityCol = col + 1;
        PrimordialEntityModel entityModel = entityModelMatrix[entityRow][entityCol];
        if (entityModel != null) {
          boolean nearbyFood = isFoodNearby(row, col);
          boolean nearbyEntity = isEntityNearby(row, col);
          int frontEntityTypeOrdinal = getCellTypeInDirection(row, col,
              entityModel.getDirection());
          inputMatrix[row][col].setInput(entityModel.getEnergy(), entityModel.getMaxEnergy(),
              frontEntityTypeOrdinal, nearbyEntity, nearbyFood);
        }
      }
    }
  }

  public boolean isValidCell(int row, int col) {
    return row >= 0 && col >= 0 && row < getHeight() && col < getWidth();
  }

  public int getCellTypeInDirection(int row, int col, Direction direction) {
    int directionOrdinal = direction.ordinal();
    int frontRow = row + CELL_DIRECTION_ROW_OFFSETS[directionOrdinal] + 1;
    int frontCol = col + CELL_DIRECTION_COL_OFFSETS[directionOrdinal] + 1;
    if (entityModelMatrix[frontRow][frontCol] != null) {
      return ENTITY_ORDINAL;
    } else if (foodMatrix[frontRow][frontCol] != 0) {
      return FOOD_ORDINAL;
    }
    return NONE_ORDINAL;
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

  public boolean moveEntityToCellInDirection(int fromRow, int fromCol, EntityModel<EntityType> entityModel) {
    int directionOrdinal = entityModel.getDirection().ordinal();
    int toRow = fromRow + CELL_DIRECTION_ROW_OFFSETS[directionOrdinal];
    int toCol = fromCol + CELL_DIRECTION_COL_OFFSETS[directionOrdinal];

    if(moveEntityToCell(fromRow, fromCol, toRow, toCol)) {
      entityModel.setY(toRow);
      entityModel.setX(toCol);
      return true;
    }
    return false;
  }

  public boolean moveEntityToCell(int fromRow, int fromCol, int toRow, int toCol) {
    if (!isValidCell(toRow, toCol)) {
      return false;
    }
    toRow++;
    toCol++;
    if (entityModelMatrix[toRow][toCol] != null) {
      return false;
    }
    fromRow++;
    fromCol++;
    assert entityModelMatrix[fromRow][fromCol] != null;
    assert entityMatrix[fromRow][fromCol] == 1;
    assert entityModelMatrix[toRow][toCol] == null;
    assert entityMatrix[toRow][toCol] == 0;

    entityModelMatrix[toRow][toCol] = entityModelMatrix[fromRow][fromCol];
    entityModelMatrix[fromRow][fromCol] = null;
    entityMatrix[fromRow][fromCol] = 0;
    entityMatrix[toRow][toCol] = 1;
    return true;
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

  public boolean isEntityNearby(int row, int col) {
    row++;
    col++;
    int sum = 0;
    sum += entityMatrix[row][col];
    sum += entityMatrix[row + 1][col];
    sum += entityMatrix[row + 1][col - 1];
    sum += entityMatrix[row + 1][col + 1];
    sum += entityMatrix[row - 1][col];
    sum += entityMatrix[row - 1][col - 1];
    sum += entityMatrix[row - 1][col + 1];
    return sum > 0;
  }

  public boolean isFoodNearby(int row, int col) {
    row++;
    col++;
    int sum = 0;
    sum += foodMatrix[row][col];
    sum += foodMatrix[row + 1][col];
    sum += foodMatrix[row + 1][col - 1];
    sum += foodMatrix[row + 1][col + 1];
    sum += foodMatrix[row - 1][col];
    sum += foodMatrix[row - 1][col - 1];
    sum += foodMatrix[row - 1][col + 1];
    return sum > 0;
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
    inputMatrix = new PrimordialEntityInput[getHeight()][];
    // Allocate cells.
    for (int row = 0; row < entityMatrix.length; row++) {
      entityMatrix[row] = new int[getWidth()+2];
      entityModelMatrix[row] = new PrimordialEntityModel[getWidth()+2];
      foodMatrix[row] = new int[getWidth()+2];
    }

    for(int row = 0; row < inputMatrix.length; row++) {
      inputMatrix[row] = new PrimordialEntityInput[getWidth()];
      for(int col = 0; col < getWidth(); col++) {
        inputMatrix[row][col] = new PrimordialEntityInput();
      }
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
