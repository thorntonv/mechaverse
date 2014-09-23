package org.mechaverse.simulation.common;


/**
 * Represents a simulation state.
 *
 * @param <M> The simulation model type
 */
public abstract class SimulationState<M> extends SimulationDataStore {

  protected final M model;

  public SimulationState(M model, SimulationDataStore dataStore) {
    super(dataStore);

    this.model = model;
  }

  /**
   * Returns the simulation id.
   */
  public abstract String getId();

  /**
   * Returns the current iteration.
   */
  public abstract long getIteration();

  public M getModel() {
    return model;
  }
}
