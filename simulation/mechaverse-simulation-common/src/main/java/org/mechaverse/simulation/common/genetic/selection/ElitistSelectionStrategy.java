package org.mechaverse.simulation.common.genetic.selection;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import gnu.trove.map.TObjectDoubleMap;

public class ElitistSelectionStrategy<E> implements SelectionStrategy<E> {

  private final int retainTopEntityCount;
  private final int removeBottomEntityCount;
  private final SelectionStrategy<E> selectionStrategy;

  public ElitistSelectionStrategy(int retainTopEntityCount, int removeBottomEntityCount,
      SelectionStrategy<E> selectionStrategy) {
    this.retainTopEntityCount = retainTopEntityCount;
    this.removeBottomEntityCount = removeBottomEntityCount;
    this.selectionStrategy = selectionStrategy;
  }

  @Override
  public List<Pair<E, E>> selectEntities(
      TObjectDoubleMap<E> entityFitnessMap, int count, RandomGenerator random) {
    if (retainTopEntityCount == 0 && removeBottomEntityCount == 0) {
      return selectionStrategy.selectEntities(entityFitnessMap, count, random);
    }

    int retainCount = Math.min(retainTopEntityCount, count);
    List<E> eliteEntities = SelectionUtil.filterAndSelectEliteEntities(
      retainCount, removeBottomEntityCount, entityFitnessMap, getMinimize());

    List<Pair<E, E>> selectedPairs = new ArrayList<>();

    for (E entity : eliteEntities) {
      selectedPairs.add(new Pair<E, E>(entity, null));
    }

    selectedPairs.addAll(selectionStrategy.selectEntities(
        entityFitnessMap, count - selectedPairs.size(), random));

    return selectedPairs;
  }

  @Override
  public void setMinimize(boolean minimize) {
    selectionStrategy.setMinimize(minimize);
  }

  @Override
  public boolean getMinimize() {
    return selectionStrategy.getMinimize();
  }
}
