package org.mechaverse.simulation.common.datastore;

import java.util.Arrays;

/**
 * A base class for {@link SimulationDataStore} implementations.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class AbstractSimulationDataStore implements SimulationDataStore {

  @Override
  public void merge(SimulationDataStore fromDataStore) {
    for (String key : fromDataStore.keySet()) {
      put(key, fromDataStore.get(key));
    }
  }

  @Override
  public boolean equals(Object otherObject) {
    if (otherObject == null) {
      return false;
    } else if (otherObject == this) {
      return true;
    } else if (!(otherObject instanceof SimulationDataStore)) {
      return false;
    }

    SimulationDataStore otherDataStore = (SimulationDataStore) otherObject;
    if (!otherDataStore.keySet().equals(keySet())) {
      return false;
    }
    for (String key : keySet()) {
      if (!Arrays.equals(get(key), otherDataStore.get(key))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(SimulationDataStoreOutputStream.toByteArray(this));
  }
}
