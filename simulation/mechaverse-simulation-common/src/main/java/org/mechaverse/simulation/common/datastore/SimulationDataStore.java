package org.mechaverse.simulation.common.datastore;

import java.util.Set;

/**
 * A data store for simulation key/value data.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface SimulationDataStore {

  public static String KEY_SEPARATOR = ".";

  /**
   * Returns the data associated with the given key.
   *
   * @param key the key of the data to return
   * @return the data associated with the given key or null if the data does not exist
   */
  public byte[] get(String key);

  /**
   * Associates data with the given key.
   *
   * @param key the key of the data
   * @param the data to associate with the given key
   */
  public void put(String key, byte[] value);

  /**
   * Merges values from the given data store into this data store. Existing values in this data
   * store whose keys match keys in the given data store will be overwritten.
   */
  public void merge(SimulationDataStore fromDataStore);

  /**
   * Removes the data associated with the given key.
   *
   * @param key the key of the data to remove
   */
  public void remove(String key);

  /**
   * Removes all data in this data store.
   */
  public void clear();

  /**
   * Removes the data associated with the given key.
   *
   * @param key the key of the data to remove
   */
  public boolean containsKey(String key);

  /**
   * Returns the set of all keys with associated data.
   */
  public Set<String> keySet();

  /**
   * Returns the set of keys that start with the given prefix.
   */
  public Set<String> keysWithPrefix(String prefix);

  /**
   * Returns the total number of key/value pairs.
   */
  public int size();

  /**
   * Returns true if the given object is a {@link SimulationDataStore} instance that has the same
   * content as this instance.
   */
  @Override
  public boolean equals(Object otherObject);
}
