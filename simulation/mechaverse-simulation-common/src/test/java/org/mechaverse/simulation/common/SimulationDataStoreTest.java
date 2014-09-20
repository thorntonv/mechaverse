package org.mechaverse.simulation.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * A unit test of {@link SimulationDataStore}.
 */
public class SimulationDataStoreTest {

  private Random random;
  private SimulationDataStore dataStore;

  @Before
  public void setUp() {
    dataStore = new SimulationDataStore();
    random = new Random(SimulationDataStoreTest.class.getName().hashCode());
  }

  @Test
  public void testPutGet() {
    dataStore.put("testKey", "testValue".getBytes());
    assertArrayEquals("testValue".getBytes(), dataStore.get("testKey"));
  }

  @Test
  public void testRemove() {
    dataStore.put("testKey", "testValue".getBytes());
    dataStore.remove("testKey");
    assertFalse(dataStore.containsKey("testKey"));
    assertEquals(0, dataStore.keySet().size());
    assertEquals(null, dataStore.get("testKey"));
  }

  @Test
  public void testSerializeDeserialize() throws IOException {
    byte[][] testValues = new byte[1024][];
    for (int idx = 0; idx < testValues.length; idx++) {
      byte[] testValue = new byte[idx+1];
      random.nextBytes(testValue);
      dataStore.put(getTestKey(idx+1), testValue);
      testValues[idx] = testValue;
    }

    byte[] serialized = dataStore.serialize();
    assertEquals(1024 * 1025 / 2 + (4 + 2 + 4) * testValues.length + 4, serialized.length);

    dataStore = SimulationDataStore.deserialize(serialized);
    assertEquals(testValues.length, dataStore.size());
    for (int idx = 0; idx < testValues.length; idx++) {
      byte[] expectedValue = testValues[idx];
      assertArrayEquals(expectedValue, dataStore.get(getTestKey(idx+1)));
    }
  }

  private String getTestKey(int cnt) {
    return String.format("%04d", cnt);
  }
}
