package org.mechaverse.simulation.common.genetic.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Function;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class SelectionUtil {

  public static <E> TObjectDoubleMap<E> buildEntityFitnessMap(
      List<E> entities, Function<E, Double> entityFitnessFunction) {
    TObjectDoubleMap<E> fitnessMap = new TObjectDoubleHashMap<>();
    for (E entity : entities) {
      fitnessMap.put(entity, entityFitnessFunction.apply(entity));
    }
    return fitnessMap;
  }

  public static <E> List<E> filterAndSelectEliteEntities(int retainTopEntityCount,
      int removeBottomEntityCount, TObjectDoubleMap<E> entityFitnessMap, boolean minimize) {
    return filterAndSelectEliteEntities(retainTopEntityCount, removeBottomEntityCount,
        getEntitiesSortedByFitness(entityFitnessMap, minimize), entityFitnessMap);
  }

  /**
   * Returns a list of the most fit entities and filters out the least fit entities.
   *
   * @param retainTopEntityCount the number of most fit entities to return
   * @param removeBottomEntityCount the number of least fit entities to filter out
   * @param sortedEntityList the list of entities sorted by fitness
   * @param entityFitnessMap maps each entity to its fitness
   * @return a list of the most fit entities
   */
  public static <E> List<E> filterAndSelectEliteEntities(int retainTopEntityCount,
      int removeBottomEntityCount, List<E> sortedEntityList, TObjectDoubleMap<E> entityFitnessMap) {
    int removeCount = Math.min(removeBottomEntityCount, sortedEntityList.size());
    for (int cnt = 1; cnt <= removeCount; cnt++) {
      E entity = sortedEntityList.remove(sortedEntityList.size() - 1);
      entityFitnessMap.remove(entity);
    }

    int retainCount = Math.min(retainTopEntityCount, sortedEntityList.size());

    List<E> selectedEntities = new ArrayList<>();
    for (int idx = 0; idx < retainCount; idx++) {
      selectedEntities.add(sortedEntityList.get(idx));
    }
    return selectedEntities;
  }

  public static <E> List<E> getEntitiesSortedByFitness(
      final TObjectDoubleMap<E> entityFitnessMap, boolean minimize) {
    List<E> entities = new ArrayList<E>(entityFitnessMap.keySet());
    Collections.sort(entities, newEntityComparator(entityFitnessMap, minimize));
    return entities;
  }

  public static <E> Comparator<E> newEntityComparator(
      final TObjectDoubleMap<E> fitnessMap, final boolean minimize) {
    return new Comparator<E>() {
      @Override
      public int compare(E entity, E otherEntity) {
        return minimize ? Double.compare(fitnessMap.get(entity), fitnessMap.get(otherEntity)) :
            Double.compare(fitnessMap.get(otherEntity), fitnessMap.get(entity));
      }
    };
  }
}
