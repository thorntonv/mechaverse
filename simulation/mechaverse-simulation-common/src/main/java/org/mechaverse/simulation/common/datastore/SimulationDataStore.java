package org.mechaverse.simulation.common.datastore;

import java.util.Set;

/**
 * A data store for simulation key/value data.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface SimulationDataStore {

  String KEY_SEPARATOR = ".";

  /**
   * Returns the data associated with the given key.
   *
   * @param key the key of the data to return
   * @return the data associated with the given key or null if the data does not exist
   */
  byte[] get(String key);

  /**
   * Associates data with the given key.
   *
   * @param key the key of the data
   * @param value the data to associate with the given key
   */
  void put(String key, byte[] value);

  /**
   * Merges values from the given data store into this data store. Existing values in this data
   * store whose keys match keys in the given data store will be overwritten.
   */
  void merge(SimulationDataStore fromDataStore);

  /**
   * Removes the data associated with the given key.
   *
   * @param key the key of the data to remove
   */
  void remove(String key);

  /**
   * Removes all data in this data store.
   */
  void clear();

  /**
   * Removes the data associated with the given key.
   *
   * @param key the key of the data to remove
   */
  boolean containsKey(String key);

  /**
   * Returns the set of all keys with associated data.
   */
  Set<String> keySet();

  /**
   * Returns the total number of key/value pairs.
   */
  int size();

  /**
   * Returns true if the given object is a {@link SimulationDataStore} instance that has the same
   * content as this instance.
   */
  @Override
  boolean equals(Object otherObject);
}
