package org.mechaverse.simulation.common.genetic.selection;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import com.google.common.base.Preconditions;

import gnu.trove.map.TObjectDoubleMap;

/**
 * A {@link SelectionStrategy} implementation that selects parent entities using tournament
 * selection.
 */
public class TournamentSelectionStrategy<E> extends AbstractSelectionStrategy<E> {

  private static final double DEFAULT_K = .75;
  
  private final double k;

  public TournamentSelectionStrategy() {
    this(DEFAULT_K);
  }

  public TournamentSelectionStrategy(double k) {
    this.k = k;
  }

  @Override
  public List<Pair<E, E>> selectEntities(
      TObjectDoubleMap<E> entityFitnessMap, int entityCount, RandomGenerator random) {
    Preconditions.checkNotNull(entityFitnessMap);
    Preconditions.checkNotNull(random);

    List<E> entities = new ArrayList<>(entityFitnessMap.keySet());

    List<Pair<E, E>> selectedPairs = new ArrayList<>();
    while (selectedPairs.size() < entityCount) {
      E parent1 = selectParent(entities, entityFitnessMap, random);
      E parent2 = selectParent(entities, entityFitnessMap, random);
      selectedPairs.add(new Pair<>(parent1, parent2));
    }

    return selectedPairs;
  }

  private E selectParent(List<E> entities, final TObjectDoubleMap<E> fitnessMap,
      RandomGenerator random) {
    E parent1 = entities.get(random.nextInt(entities.size()));
    E parent2 = entities.get(random.nextInt(entities.size()));

    if ((minimize && fitnessMap.get(parent1) > fitnessMap.get(parent2)) ||
        !minimize && fitnessMap.get(parent1) < fitnessMap.get(parent2)) {
      E temp = parent1;
      parent1 = parent2;
      parent2 = temp;
    }
    return (random.nextDouble() < k) ? parent1 : parent2;
  }
}
