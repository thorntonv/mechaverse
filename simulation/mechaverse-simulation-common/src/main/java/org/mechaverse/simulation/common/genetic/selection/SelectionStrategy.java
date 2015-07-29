package org.mechaverse.simulation.common.genetic.selection;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import gnu.trove.map.TObjectDoubleMap;

/**
 * A strategy for selecting pairs of entities that will mate to form the next generation.
 */
public interface SelectionStrategy<E> {

  /**
   * Selects pairs of parent entities.
   *
   * @param entityFitnessMap a map that maps each entity to its fitness
   * @param count the number of pairs to select
   */
  List<Pair<E, E>> selectEntities(
      TObjectDoubleMap<E> entityFitnessMap, int count, RandomGenerator random);

  void setMinimize(boolean minimize);
  boolean getMinimize();
}