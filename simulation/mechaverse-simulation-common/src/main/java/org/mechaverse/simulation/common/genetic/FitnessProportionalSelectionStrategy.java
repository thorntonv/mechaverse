package org.mechaverse.simulation.common.genetic;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * A {@link SelectionStrategy} implementation that selects parent entities proportionally based on
 * their fitness.
 */
public class FitnessProportionalSelectionStrategy<E> implements SelectionStrategy<E> {

  @Override
  public EntitySelectionInfo<E> selectEntities(List<E> entities,
      Function<E, Double> fitnessFunction, RandomGenerator random) {
    Preconditions.checkNotNull(entities);
    Preconditions.checkNotNull(fitnessFunction);
    Preconditions.checkArgument(entities.size() >= 2);

    EnumeratedDistribution<E> distribution =
        getFitnessDistribution(getEntityFitnessMap(entities, fitnessFunction), random);

    List<Pair<E, E>> selectedPairs = new ArrayList<>();
    Set<E> notSelected = Sets.newIdentityHashSet();
    notSelected.addAll(entities);

    if (distribution != null) {
      for (int cnt = 1; cnt <= entities.size(); cnt++) {
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
    return new EntitySelectionInfo<E>(selectedPairs, notSelected);
  }

  private IdentityHashMap<E, Double> getEntityFitnessMap(
      List<E> entities, Function<E, Double> fitnessFunction) {
    IdentityHashMap<E, Double> entityFitnessMap = new IdentityHashMap<>(entities.size());
    for (E entity : entities) {
      double fitness = fitnessFunction.apply(entity);
      if (fitness > 0) {
        entityFitnessMap.put(entity, fitness);
      }
    }
    return entityFitnessMap;
  }

  private EnumeratedDistribution<E> getFitnessDistribution(
      IdentityHashMap<E, Double> entityFitnessMap, RandomGenerator random) {
    if (entityFitnessMap.isEmpty()) {
      return null;
    }
    double fitnessSum = 0;
    for (Double fitness : entityFitnessMap.values()) {
      fitnessSum += fitness;
    }

    List<Pair<E, Double>> pmf = new ArrayList<>();
    for (Map.Entry<E, Double> entry : entityFitnessMap.entrySet()) {
      E entity = entry.getKey();
      if (fitnessSum != 0) {
        pmf.add(new Pair<E, Double>(entity, entry.getValue()));
      } else {
        pmf.add(new Pair<E, Double>(entity, 1.0D / entityFitnessMap.size()));
      }
    }

    return new EnumeratedDistribution<E>(random, pmf);
  }
}