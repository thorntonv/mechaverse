package org.mechaverse.simulation.common.datastore;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * A unit test for {@link SimulationDataStoreView}.
 */
@SuppressWarnings("WeakerAccess")
@RunWith(MockitoJUnitRunner.class)
public class SimulationDataStoreViewTest {

  private static final String ROOT_KEY = "test";

  @Mock SimulationDataStore mockDataStore;
  @Mock
  Predicate<String> mockVisibleKeyPredicate;

  private SimulationDataStoreView dataStoreView;

  @Before
  public void setUp() {
    dataStoreView = new SimulationDataStoreView(ROOT_KEY, mockVisibleKeyPredicate, mockDataStore);
  }

  @Test
  public void get_existing() {
    byte[] expectedData = "data".getBytes();
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("existingKey"))).thenReturn(true);
    when(mockDataStore.get(getAbsoluteKey("existingKey"))).thenReturn(expectedData);
    assertArrayEquals(expectedData, dataStoreView.get("existingKey"));
  }

  @Test
  public void get_notVisible() {
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("notVisibleKey"))).thenReturn(false);
    assertNull(dataStoreView.get("notVisibleKey"));
  }

  @Test
  public void get_notExistent() {
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("notExistentKey"))).thenReturn(true);
    assertNull(dataStoreView.get("notExistentKey"));
  }

  @Test
  public void put() {
    byte[] data = "newData".getBytes();
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("newKey"))).thenReturn(true);
    dataStoreView.put("newKey", data);
    verify(mockDataStore).put(getAbsoluteKey("newKey"), data);
  }

  @Test
  public void put_notVisible() {
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("newKey"))).thenReturn(false);
    try {
      dataStoreView.put("newKey", "newData".getBytes());
      fail("Expected exception was not thrown.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }
    verifyZeroInteractions(mockDataStore);
  }

  @Test
  public void remove() {
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("existingKey"))).thenReturn(true);
    dataStoreView.remove("existingKey");
    verify(mockDataStore).remove(getAbsoluteKey("existingKey"));
  }

  @Test
  public void remove_notVisible() {
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("existingKey"))).thenReturn(false);
    try {
      dataStoreView.remove("existingKey");
      fail("Expected exception was not thrown.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }
    verifyZeroInteractions(mockDataStore);
  }

  @Test
  public void clear() {
    String rootPrefix = ROOT_KEY + SimulationDataStore.KEY_SEPARATOR;
    when(mockDataStore.keysWithPrefix(rootPrefix)).thenReturn(
        ImmutableSet.of(getAbsoluteKey("key1"), getAbsoluteKey("key2")));
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("key1"))).thenReturn(true);
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("key2"))).thenReturn(true);

    dataStoreView.clear();
    verify(mockDataStore, times(2)).remove(any(String.class));
    verify(mockDataStore).remove(getAbsoluteKey("key1"));
    verify(mockDataStore).remove(getAbsoluteKey("key2"));
  }

  @Test
  public void containsKey_existing() {
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("existingKey"))).thenReturn(true);
    when(mockDataStore.containsKey(getAbsoluteKey("existingKey"))).thenReturn(true);
    assertTrue(dataStoreView.containsKey("existingKey"));
  }

  @Test
  public void containsKey_notExistent() {
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("notExistentKey"))).thenReturn(true);
    when(mockDataStore.containsKey(getAbsoluteKey("notExistentKey"))).thenReturn(false);
    assertFalse(dataStoreView.containsKey(getAbsoluteKey("notExistentKey")));
  }

  @Test
  public void containsKey_notVisible() {
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("notVisibleKey"))).thenReturn(false);
    assertFalse(dataStoreView.containsKey(getAbsoluteKey("notVisibleKey")));
    verifyZeroInteractions(mockDataStore);
  }

  @Test
  public void keySet() {
    String rootPrefix = ROOT_KEY + SimulationDataStore.KEY_SEPARATOR;
    when(mockDataStore.keysWithPrefix(eq(rootPrefix))).thenReturn(ImmutableSet.of(
        getAbsoluteKey("visibleKey1"), getAbsoluteKey("visibleKey2.subKey"),
            getAbsoluteKey("notVisibleKey")));
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("visibleKey1"))).thenReturn(true);
    when(mockVisibleKeyPredicate.test(getAbsoluteKey("visibleKey2.subKey"))).thenReturn(true);
    assertEquals(ImmutableSet.of("visibleKey1", "visibleKey2.subKey"), dataStoreView.keySet());
    verify(mockVisibleKeyPredicate).test(getAbsoluteKey("notVisibleKey"));
    verify(mockVisibleKeyPredicate, never()).test("otherRootKey");
  }

  private String getAbsoluteKey(String key) {
    return ROOT_KEY + SimulationDataStore.KEY_SEPARATOR + key;
  }
}
