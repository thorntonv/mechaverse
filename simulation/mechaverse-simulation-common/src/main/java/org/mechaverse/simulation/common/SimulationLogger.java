package org.mechaverse.simulation.common;

import java.util.List;

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
}
