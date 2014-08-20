package org.mechaverse.simulation.common;

/**
 * Represents a simulation state.
 *
 * @param <M> The simulation model type
 */
public abstract class SimulationState<M> {

  protected final M model;
  protected final SimulationDataStore dataStore;

  public SimulationState(M model) {
    this(model, new SimulationDataStore());
  }

  public SimulationState(M model, SimulationDataStore dataStore) {
    this.model = model;
    this.dataStore = dataStore;
  }

  /**
   * Returns the simulation id.
   */
  public abstract String getId();

  /**
   * Returns the id of the simulation instance.
   */
  public abstract String getInstanceId();

  /**
   * Returns the current iteration.
   */
  public abstract long getIteration();

  public M getModel() {
    return model;
  }

  public byte[] getData(String key) {
    return dataStore.get(key);
  }

  public void setData(String key, byte[] value) {
    dataStore.put(key, value);
  }
}
