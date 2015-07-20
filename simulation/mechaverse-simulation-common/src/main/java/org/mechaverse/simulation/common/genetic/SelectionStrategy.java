package org.mechaverse.simulation.common.genetic;

import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import com.google.common.base.Function;

/**
 * A strategy for selecting pairs of entities that will mate to form the next generation.
 */
public interface SelectionStrategy<E> {

  public static class EntitySelectionInfo<E> {

    private final List<Pair<E, E>> selectedPairs;
    private final Set<E> entitiesNotSelected;

    public EntitySelectionInfo(List<Pair<E, E>> selectedPairs, Set<E> entitiesNotSelected) {
      this.selectedPairs = selectedPairs;
      this.entitiesNotSelected = entitiesNotSelected;
    }

    public List<Pair<E, E>> getSelectedPairs() {
      return selectedPairs;
    }

    public Set<E> getEntitiesNotSelected() {
      return entitiesNotSelected;
    }
  }

  EntitySelectionInfo<E> selectEntities(List<E> entities, Function<E, Double> fitnessFunction,
      RandomGenerator random);
}
