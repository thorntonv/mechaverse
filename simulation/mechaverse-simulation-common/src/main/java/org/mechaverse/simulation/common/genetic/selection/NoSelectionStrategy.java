package org.mechaverse.simulation.common.genetic.selection;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import gnu.trove.map.TObjectDoubleMap;

/**
 * A {@link SelectionStrategy} implementation that does not select any entities.
 */
public class NoSelectionStrategy<E> extends AbstractSelectionStrategy<E> {

  @Override
  public List<Pair<E, E>> selectEntities(TObjectDoubleMap<E> entityFitnessMap, int count,
      RandomGenerator random) {
    return new ArrayList<Pair<E, E>>();
  }
}
