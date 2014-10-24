package org.mechaverse.simulation.common.datastore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A {@link SimulationDataStore} implementation that stores data in memory.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class MemorySimulationDataStore implements SimulationDataStore {

  // TODO(thorntonv): Move the serialize/deserialize methods to separate classes.

  private final Map<String, byte[]> dataStore;

  public static void deserialize(InputStream in, Map<String, byte[]> targetMap)
      throws IOException {
    try (DataInputStream dataIn = new DataInputStream(in)) {
      int size = dataIn.readInt();
      for (int cnt = 1; cnt <= size; cnt++) {
        String key = dataIn.readUTF();
        int dataLength = dataIn.readInt();
        byte[] value = new byte[dataLength];
        dataIn.readFully(value);
        targetMap.put(key, value);
      }
    }
  }

  public static MemorySimulationDataStore deserialize(byte[] data) throws IOException {
    return deserialize(new ByteArrayInputStream(data));
  }

  public static MemorySimulationDataStore deserialize(InputStream in) throws IOException {
    MemorySimulationDataStore dataStore = new MemorySimulationDataStore();
    deserialize(in, dataStore.dataStore);
    return dataStore;
  }

  public MemorySimulationDataStore() {
    this.dataStore = new LinkedHashMap<>();
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
  public boolean containsKey(String key) {
    return dataStore.containsKey(key);
  }

  @Override
  public Set<String> keySet() {
    return dataStore.keySet();
  }

  public byte[] serialize() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    serialize(byteOut);
    byteOut.close();
    return byteOut.toByteArray();
  }

  public void serialize(OutputStream out) throws IOException {
    try (DataOutputStream dataOut = new DataOutputStream(out)) {
      dataOut.writeInt(dataStore.size());
      for (Entry<String, byte[]> entry : dataStore.entrySet()) {
        dataOut.writeUTF(entry.getKey());
        dataOut.writeInt(entry.getValue().length);
        dataOut.write(entry.getValue());
      }
    }
  }

  @Override
  public int size() {
    return dataStore.size();
  }
}
