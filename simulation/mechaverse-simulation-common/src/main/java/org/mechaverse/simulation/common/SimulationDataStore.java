package org.mechaverse.simulation.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A data store for simulation key/value data.
 */
public final class SimulationDataStore {

  private final Map<String, byte[]> dataStore = new HashMap<>();

  public static void deserialize(byte[] data, Map<String, byte[]> targetMap) throws IOException {
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

    int size = in.readInt();
    for (int cnt = 1; cnt <= size; cnt++) {
      String key = in.readUTF();
      int dataLength = in.readInt();
      byte[] value = new byte[dataLength];
      in.read(value);
      targetMap.put(key, value);
    }
  }

  public static SimulationDataStore deserialize(byte[] data) throws IOException {
    SimulationDataStore dataStore = new SimulationDataStore();
    deserialize(data, dataStore.dataStore);
    return dataStore;
  }

  public byte[] get(String key) {
    return dataStore.get(key);
  }

  public void put(String key, byte[] value) {
    dataStore.put(key, value);
  }

  public boolean containsKey(String key) {
    return dataStore.containsKey(key);
  }

  public Set<String> keySet() {
    return dataStore.keySet();
  }

  public byte[] serialize() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(byteOut);

    out.writeInt(dataStore.size());
    for (Entry<String, byte[]> entry : dataStore.entrySet()) {
      out.writeUTF(entry.getKey());
      out.writeInt(entry.getValue().length);
      out.write(entry.getValue());
    }

    return byteOut.toByteArray();
  }

  public int size() {
    return dataStore.size();
  }
}
