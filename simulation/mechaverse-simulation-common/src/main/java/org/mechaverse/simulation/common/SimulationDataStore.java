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

  public byte[] get(String key) {
    return dataStore.get(key);
  }

  public void put(String key, byte[] value) {
    dataStore.put(key, value);
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

  public void deserialize(byte[] data) throws IOException {
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

    dataStore.clear();
    int size = in.readInt();
    for (int cnt = 1; cnt <= size; cnt++) {
      String key = in.readUTF();
      int dataLength = in.readInt();
      byte[] value = new byte[dataLength];
      in.read(value);
      dataStore.put(key, value);
    }
  }

  public int size() {
    return dataStore.size();
  }
}
