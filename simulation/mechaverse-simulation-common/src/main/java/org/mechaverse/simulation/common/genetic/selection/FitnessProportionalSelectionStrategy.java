package org.mechaverse.simulation.common.genetic.selection;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import com.google.common.base.Preconditions;

import gnu.trove.map.TObjectDoubleMap;

/**
 * A {@link SelectionStrategy} implementation that selects parent entities proportionally based on
 * their fitness.
 */
public class FitnessProportionalSelectionStrategy<E> extends AbstractSelectionStrategy<E> {

  @Override
  public List<Pair<E, E>> selectEntities(TObjectDoubleMap<E> entityFitnessMap, int count,
      RandomGenerator random) {
    Preconditions.checkNotNull(entityFitnessMap);
    Preconditions.checkNotNull(random);

    EnumeratedDistribution<E> distribution = getFitnessDistribution(entityFitnessMap, random);

    List<Pair<E, E>> selectedPairs = new ArrayList<>();

    if (distribution != null) {
      for (int cnt = 1; cnt <= count; cnt++) {
        E parent1 = distribution.sample();
        E parent2 = distribution.sample();

        if (distribution.getPmf().size() > 1) {
          // Ensure that parent1 != parent2.
          while (parent2 == parent1) {
            parent2 = distribution.sample();
          }
        }
        selectedPairs.add(new Pair<>(parent1, parent2));
      }
    }
    return selectedPairs;
  }

  private EnumeratedDistribution<E> getFitnessDistribution(
      final TObjectDoubleMap<E> entityFitnessMap, RandomGenerator random) {
    if (entityFitnessMap.isEmpty()) {
      return null;
    }
    double sum = 0;
    for (Double fitness : entityFitnessMap.values()) {
      sum += fitness;
    }

    final double fitnessSum = sum;
    final List<Pair<E, Double>> pmf = new ArrayList<>();
    entityFitnessMap.forEachEntry((entity, value) -> {
      if (fitnessSum != 0) {
        pmf.add(new Pair<>(entity, value));
      } else {
        pmf.add(new Pair<>(entity, 1.0D / entityFitnessMap.size()));
      }
      return true;
    });

    return new EnumeratedDistribution<>(random, pmf);
  }
}
