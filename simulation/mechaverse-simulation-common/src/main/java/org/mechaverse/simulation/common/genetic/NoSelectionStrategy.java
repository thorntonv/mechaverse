package org.mechaverse.simulation.common.genetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

/**
 * A {@link SelectionStrategy} implementation that does not select any entities.
 */
public class NoSelectionStrategy<E> implements SelectionStrategy<E> {

  @Override
  public EntitySelectionInfo<E> selectEntities(List<E> entities,
      Function<E, Double> fitnessFunction, RandomGenerator random) {
    Set<E> notSelected = Sets.newIdentityHashSet();
    notSelected.addAll(entities);
    return new EntitySelectionInfo<>(new ArrayList<Pair<E, E>>(), notSelected);
  }
}
