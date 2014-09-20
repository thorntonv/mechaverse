package org.mechaverse.simulation.common;

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
 * A data store for simulation key/value data.
 */
public final class SimulationDataStore {

  private final Map<String, byte[]> dataStore = new LinkedHashMap<>();

  public static void deserialize(InputStream in, Map<String, byte[]> targetMap)
      throws IOException {
    try (DataInputStream dataIn = new DataInputStream(in)) {
      int size = dataIn.readInt();
      for (int cnt = 1; cnt <= size; cnt++) {
        String key = dataIn.readUTF();
        int dataLength = dataIn.readInt();
        byte[] value = new byte[dataLength];
        in.read(value);
        targetMap.put(key, value);
      }
    }
  }

  public static SimulationDataStore deserialize(byte[] data) throws IOException {
    return deserialize(new ByteArrayInputStream(data));
  }

  public static SimulationDataStore deserialize(InputStream in) throws IOException {
    SimulationDataStore dataStore = new SimulationDataStore();
    deserialize(in, dataStore.dataStore);
    return dataStore;
  }

  public byte[] get(String key) {
    return dataStore.get(key);
  }

  public void put(String key, byte[] value) {
    dataStore.put(key, value);
  }

  public void remove(String key) {
    dataStore.remove(key);
  }

  public boolean containsKey(String key) {
    return dataStore.containsKey(key);
  }

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

  public int size() {
    return dataStore.size();
  }
}
