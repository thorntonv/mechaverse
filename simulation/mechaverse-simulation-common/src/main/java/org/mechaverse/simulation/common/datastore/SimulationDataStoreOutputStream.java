package org.mechaverse.simulation.common.datastore;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A {@link DataOutputStream} that can be used to serialize a {@link SimulationDataStore}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class SimulationDataStoreOutputStream extends DataOutputStream {

  /**
   * Returns the serialized data of the given {@link SimulationDataStore} as a byte array.
   */
  public static byte[] toByteArray(SimulationDataStore dataStore) {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream(getSerializedSizeInBytes(dataStore));
    try (SimulationDataStoreOutputStream out = new SimulationDataStoreOutputStream(byteOut)) {
      out.writeDataStore(dataStore);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return byteOut.toByteArray();
  }

  /**
   * Returns the serialized size of the given {@link SimulationDataStore} in bytes.
   */
  public static int getSerializedSizeInBytes(SimulationDataStore dataStore) {
    // Number of entries.
    int size = 4;
    for (String key : dataStore.keySet()) {
      // Key string and length.
      size += key.length() + 2;
      // Data length.
      size += 4;
      // Data.
      size += dataStore.get(key).length;
    }
    return size;
  }

  public SimulationDataStoreOutputStream(OutputStream out) {
    super(out);
  }

  public void writeDataStore(SimulationDataStore dataStore) throws IOException {
    writeInt(dataStore.size());
    for (String key : dataStore.keySet()) {
      byte[] value = dataStore.get(key);
      writeUTF(key);
      writeInt(value.length);
      write(value);
    }
  }
}
