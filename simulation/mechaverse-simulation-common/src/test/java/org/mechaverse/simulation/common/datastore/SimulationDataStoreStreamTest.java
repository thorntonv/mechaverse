package org.mechaverse.simulation.common.datastore;

import static org.junit.Assert.assertEquals;
import static org.mechaverse.simulation.common.datastore.SimulationDataStoreOutputStream.toByteArray;

import java.io.IOException;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test of {@link SimulationDataStoreInputStream} and {@link SimulationDataStoreOutputStream}.
 */
public class SimulationDataStoreStreamTest {

  private Random random;
  private SimulationDataStore dataStore;

  @Before
  public void setUp() {
    dataStore = new MemorySimulationDataStore();
    random = new Random(SimulationDataStoreStreamTest.class.getName().hashCode());
  }

  @Test
  public void readWrite() throws IOException {
    // Create a test data store with 1024 entries. The value of entry i is an array of i random
    // values.
    byte[][] testValues = new byte[1024][];
    for (int idx = 0; idx < testValues.length; idx++) {
      byte[] testValue = new byte[idx+1];
      random.nextBytes(testValue);
      dataStore.put(getTestKey(idx+1), testValue);
      testValues[idx] = testValue;
    }

    // Total data size is sum from 1 to 1024.
    int expectedSerializedSizeInBytes = 1024 * 1025 / 2 + (4 + 2 + 4) * testValues.length + 4;
    assertEquals(expectedSerializedSizeInBytes,
        SimulationDataStoreOutputStream.getSerializedSizeInBytes(dataStore));

    byte[] serialized = toByteArray(dataStore);
    assertEquals(expectedSerializedSizeInBytes, serialized.length);
    assertEquals(dataStore, MemorySimulationDataStore.fromByteArray(serialized));
  }

  private String getTestKey(int cnt) {
    return String.format("%04d", cnt);
  }
}
