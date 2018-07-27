package org.mechaverse.simulation.common.datastore;

/**
 * A unit test for {@link MemorySimulationDataStore}.
 */
public class MemorySimulationDataStoreTest extends AbstractSimulationDataStoreTest {

  @Override
  protected SimulationDataStore newSimulationDataStore() {
    return new MemorySimulationDataStore();
  }
}
