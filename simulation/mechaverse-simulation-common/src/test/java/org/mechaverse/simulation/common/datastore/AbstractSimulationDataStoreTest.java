package org.mechaverse.simulation.common.datastore;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

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
  public void putAndGet() {
    dataStore.put("testKey", "testValue".getBytes());
    assertArrayEquals("testValue".getBytes(), dataStore.get("testKey"));
  }

  @Test
  public void remove() {
    dataStore.put("testKey", "testValue".getBytes());
    dataStore.remove("testKey");
    assertFalse(dataStore.containsKey("testKey"));
    assertEquals(0, dataStore.keySet().size());
    assertNull(dataStore.get("testKey"));
  }

  @Test
  public void clear() {
    dataStore.put("testKey1", "testValue".getBytes());
    dataStore.put("testKey2", "testValue".getBytes());
    dataStore.put("testKey3", "testValue".getBytes());

    dataStore.clear();
    assertEquals(0, dataStore.size());
    assertEquals(Collections.emptySet(), dataStore.keySet());
    assertNull(dataStore.get("testKey1"));
    assertNull(dataStore.get("testKey2"));
    assertNull(dataStore.get("testKey3"));
  }

  @Test
  public void keySet() {
    dataStore.put("testKey1", "testValue".getBytes());
    dataStore.put("testKey2", "testValue".getBytes());
    dataStore.put("testKey3", "testValue".getBytes());

    assertEquals(ImmutableSet.of("testKey1", "testKey2", "testKey3"), dataStore.keySet());
  }
  
  @Test
  public void size() {
    dataStore.put("testKey1", "testValue".getBytes());
    dataStore.put("testKey2", "testValue".getBytes());
    dataStore.put("testKey3", "testValue".getBytes());

    assertEquals(3, dataStore.size());
  }
}
