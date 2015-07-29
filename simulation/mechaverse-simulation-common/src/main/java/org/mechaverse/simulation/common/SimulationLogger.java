package org.mechaverse.simulation.common;

import java.util.List;

import org.apache.commons.math3.util.Pair;

/**
 * An interface for simulation information loggers.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 *
 * @param <E> the entity type
 * @param <M> the model type
 */
public interface SimulationLogger<E extends AbstractEntity, M> {

  void log(long iteration, M model, List<E> entities);

  Pair<E, Double> getOverallBestEntity();

  boolean getMinimize();
  void setMinimize(boolean minimize);
}
