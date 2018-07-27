package org.mechaverse.simulation.common.datastore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * A {@link SimulationDataStore} implementation that stores data in memory.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class MemorySimulationDataStore extends AbstractSimulationDataStore {

  private static final Supplier<SimulationDataStore> SUPPLIER = MemorySimulationDataStore::new;

  /**
   * A {@link SimulationDataStoreInputStream} for reading a {@link MemorySimulationDataStore} from
   * an {@link InputStream}.
   */
  public static final class MemorySimulationDataStoreInputStream
      extends SimulationDataStoreInputStream {

    public MemorySimulationDataStoreInputStream(InputStream in) {
      super(in, SUPPLIER);
    }
  }

  /**
   * Deserialize a {@link MemorySimulationDataStore} from the data in the given byte array.
   */
  public static SimulationDataStore fromByteArray(byte[] data) throws IOException {
    ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
    try (MemorySimulationDataStoreInputStream in =
        new MemorySimulationDataStoreInputStream(byteIn)) {
      return in.readDataStore();
    }
  }

  private final TreeMap<String, byte[]> dataStore;

  public MemorySimulationDataStore() {
    this.dataStore = new TreeMap<>();
  }

  public MemorySimulationDataStore(SimulationDataStore dataStore) {
    this.dataStore = new TreeMap<>();
    merge(dataStore);
  }

  protected MemorySimulationDataStore(MemorySimulationDataStore dataStore) {
    this.dataStore = dataStore.dataStore;
  }

  @Override
  public byte[] get(String key) {
    return dataStore.get(key);
  }

  @Override
  public void put(String key, byte[] value) {
    dataStore.put(key, value);
  }

  @Override
  public void remove(String key) {
    dataStore.remove(key);
  }

  @Override
  public void clear() {
    dataStore.clear();
  }

  @Override
  public boolean containsKey(String key) {
    return dataStore.containsKey(key);
  }

  @Override
  public Set<String> keySet() {
    return dataStore.keySet();
  }

  @Override
  public int size() {
    return dataStore.size();
  }
}
