package org.mechaverse.simulation.common.datastore;

import java.io.IOException;
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
   * @throws IOException if an error occurs while retrieving the requested data
   */
  public byte[] get(String key) throws IOException;

  /**
   * Associates data with the given key.
   *
   * @param key the key of the data
   * @param the data to associate with the given key
   * @throws IOException if an error occurs while storing the requested data
   */
  public void put(String key, byte[] value) throws IOException;

  /**
   * Removes the data associated with the given key.
   *
   * @param key the key of the data to remove
   * @throws IOException if an error occurs while removing the requested data
   */
  public void remove(String key) throws IOException;

  /**
   * Removes the data associated with the given key.
   *
   * @param key the key of the data to remove
   * @throws IOException if an error occurs while removing the requested data
   */
  public boolean containsKey(String key) throws IOException;

  /**
   * Returns the set of all keys with associated data.
   *
   * @throws IOException if an error occurs while retrieving the requested data
   */
  public Set<String> keySet() throws IOException;

  /**
   * Returns the total number of key/value pairs.
   *
   * @throws IOException if an error occurs while retrieving the requested data
   */
  public int size() throws IOException;
}
