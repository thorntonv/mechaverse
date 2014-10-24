package org.mechaverse.simulation.common.datastore;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * A base unit test for {@link SimulationDataStore} implementations.
 */
public abstract class AbstractSimulationDataStoreTest {

  // TODO(thorntonv): Improve these tests.

  private SimulationDataStore dataStore;

  protected abstract SimulationDataStore newSimulationDataStore();

  @Before
  public void setUp() {
    dataStore = newSimulationDataStore();
  }

  @Test
  public void testPutGet() throws IOException {
    dataStore.put("testKey", "testValue".getBytes());
    assertArrayEquals("testValue".getBytes(), dataStore.get("testKey"));
  }

  @Test
  public void testRemove() throws IOException {
    dataStore.put("testKey", "testValue".getBytes());
    dataStore.remove("testKey");
    assertFalse(dataStore.containsKey("testKey"));
    assertEquals(0, dataStore.keySet().size());
    assertEquals(null, dataStore.get("testKey"));
  }
}
