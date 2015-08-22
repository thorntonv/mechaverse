package org.mechaverse.simulation.common.datastore;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.google.common.base.Supplier;

/**
 * A {@link DataInputStream} that can be used to read a {@link SimulationDataStore}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class SimulationDataStoreInputStream extends DataInputStream {

  /**
   * Returns an {@link InputStream} that can be used to read the serialized data of the given
   * {@link SimulationDataStore}.
   */
  public static InputStream newInputStream(final SimulationDataStore dataStore) throws IOException {
    // TODO(thorntonv): Implement unit test for this method.
    final PipedOutputStream out = new PipedOutputStream();
    new Thread(new Runnable() {
      @Override
      public void run() {
        SimulationDataStoreOutputStream dataStoreOut = new SimulationDataStoreOutputStream(out);
        try {
          dataStoreOut.writeDataStore(dataStore);
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          try {
            dataStoreOut.close();
          } catch (IOException ignored) {}
          try {
            out.close();
          } catch (IOException ignored) {}
        }
      }
    }).start();
    return new PipedInputStream(out);
  }

  private final Supplier<SimulationDataStore> dataStoreFactory;

  public SimulationDataStoreInputStream(
      InputStream in, Supplier<SimulationDataStore> dataStoreFactory) {
    super(in);
    this.dataStoreFactory = dataStoreFactory;
  }

  public SimulationDataStore readDataStore() throws IOException {
    SimulationDataStore dataStore = dataStoreFactory.get();

    int size = readInt();
    for (int cnt = 1; cnt <= size; cnt++) {
      String key = readUTF();
      int dataLength = readInt();
      byte[] value = new byte[dataLength];
      readFully(value);
      dataStore.put(key, value);
    }
    return dataStore;
  }
}
