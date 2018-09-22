package org.mechaverse.simulation.primordial.core.model;

import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;

public final class PrimordialEnvironmentModel extends
    EnvironmentModel<EntityModel<EntityType>, EntityType> {

  public static final String GENETIC_DATA_KEY = "geneticData." + CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY;

  public static final int ENTITY_MASK = 0b1;
  public static final int FOOD_ENTITY_MASK = 0b10;

  @JsonIgnore
  private byte[][] entityMatrix;
  @JsonIgnore
  private Set<EntityModel<EntityType>> entityModels = Sets.newLinkedHashSet();
  @JsonIgnore
  private Map<EntityModel<EntityType>, int[]> entityGeneticDataMap = Maps.newIdentityHashMap();

  public byte[][] getEntityMatrix() {
    return entityMatrix;
  }

  public boolean isValidCell(int row, int col) {
    return row >= 0 && col >= 0 && row < getHeight() && col < getWidth();
  }

  public void addEntity(PrimordialEntityModel entityModel) {
    int row = entityModel.getY() + 1;
    int col = entityModel.getX() + 1;
    entityMatrix[row][col] |= ENTITY_MASK;
    entityModels.add(entityModel);

    final byte[] stateBytes = entityModel.getData(GENETIC_DATA_KEY);
    if (stateBytes != null) {
      setEntityGeneticData(entityModel, ArrayUtil.toIntArray(stateBytes));
    }
  }

  public void setEntityGeneticData(EntityModel<EntityType> entityModel, int[] entityGeneticData) {
    entityGeneticDataMap.put(entityModel, entityGeneticData);
  }

  public int[] getEntityGeneticData(EntityModel<EntityType> entityModel) {
    return entityGeneticDataMap.get(entityModel);
  }

  public void removeEntityGeneticData(EntityModel<EntityType> entityModel) {
    entityGeneticDataMap.remove(entityModel);
  }

  public boolean hasEntity(int row, int col) {
    return (entityMatrix[row + 1][col + 1] & ENTITY_MASK) > 0;
  }

  @Override
  public void remove(EntityModel<EntityType> entity) {
    super.remove(entity);
    if (entity.getType() == EntityType.ENTITY && entity.getX() >= 0 && entity.getY() >= 0) {
      int row = entity.getY() + 1;
      int col = entity.getX() + 1;
      entityMatrix[row][col] &= ~ENTITY_MASK;
      entityModels.remove(entity);
      removeEntityGeneticData(entity);
    }
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
    if (entityMatrix == null) {
      initCells();
    }
    List<EntityModel<EntityType>> entities = new ArrayList<>(entityModels);
    for (int row = 0; row < entityMatrix.length; row++) {
      for (int col = 0; col < entityMatrix[row].length; col++) {
        if ((entityMatrix[row][col] & FOOD_ENTITY_MASK) > 0) {
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

    // Allocate cells.
    for (int row = 0; row < entityMatrix.length; row++) {
      entityMatrix[row] = new byte[getWidth()+2];
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
