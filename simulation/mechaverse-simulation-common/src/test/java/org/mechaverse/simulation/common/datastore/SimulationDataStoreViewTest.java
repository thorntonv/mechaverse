package org.mechaverse.simulation.common.datastore;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

/**
 * A unit test for {@link SimulationDataStoreView}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimulationDataStoreViewTest {

  private static final String ROOT_KEY = "test";

  @Mock SimulationDataStore mockDataStore;
  @Mock Predicate<String> mockVisibleKeyPredicate;

  private SimulationDataStoreView dataStoreView;

  @Before
  public void setUp() {
    dataStoreView = new SimulationDataStoreView(ROOT_KEY, mockVisibleKeyPredicate, mockDataStore);
  }

  @Test
  public void get_existing() throws IOException {
    byte[] expectedData = "data".getBytes();
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("existingKey"))).thenReturn(true);
    when(mockDataStore.get(getAbsoluteKey("existingKey"))).thenReturn(expectedData);
    assertArrayEquals(expectedData, dataStoreView.get("existingKey"));
  }

  @Test
  public void get_notVisible() throws IOException {
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("notVisbleKey"))).thenReturn(false);
    assertNull(dataStoreView.get("notVisibleKey"));
  }

  @Test
  public void get_notExistent() throws IOException {
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("notExistentKey"))).thenReturn(true);
    assertNull(dataStoreView.get("notExistentKey"));
  }

  @Test
  public void put() throws IOException {
    byte[] data = "newData".getBytes();
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("newKey"))).thenReturn(true);
    dataStoreView.put("newKey", data);
    verify(mockDataStore).put(getAbsoluteKey("newKey"), data);
  }

  @Test
  public void put_notVisible() {
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("newKey"))).thenReturn(false);
    try {
      dataStoreView.put("newKey", "newData".getBytes());
      fail("Expected exception was not thrown.");
    } catch (IOException e) {
      // Expected.
    }
    verifyZeroInteractions(mockDataStore);
  }

  @Test
  public void remove() throws IOException {
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("existingKey"))).thenReturn(true);
    dataStoreView.remove("existingKey");
    verify(mockDataStore).remove(getAbsoluteKey("existingKey"));
  }

  @Test
  public void remove_notVisible() {
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("existingKey"))).thenReturn(false);
    try {
      dataStoreView.remove("existingKey");
      fail("Expected exception was not thrown.");
    } catch (IOException e) {
      // Expected.
    }
    verifyZeroInteractions(mockDataStore);
  }

  @Test
  public void containsKey_existing() throws IOException {
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("existingKey"))).thenReturn(true);
    when(mockDataStore.containsKey(getAbsoluteKey("existingKey"))).thenReturn(true);
    assertTrue(dataStoreView.containsKey("existingKey"));
  }

  @Test
  public void containsKey_notExistent() throws IOException {
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("notExistentKey"))).thenReturn(true);
    when(mockDataStore.containsKey(getAbsoluteKey("notExistentKey"))).thenReturn(false);
    assertFalse(dataStoreView.containsKey(getAbsoluteKey("notExistentKey")));
  }

  @Test
  public void containsKey_notVisible() throws IOException {
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("notVisibleKey"))).thenReturn(false);
    assertFalse(dataStoreView.containsKey(getAbsoluteKey("notVisibleKey")));
    verifyZeroInteractions(mockDataStore);
  }

  @Test
  public void keySet() throws IOException {
    when(mockDataStore.keySet()).thenReturn(ImmutableSet.of(
      getAbsoluteKey("visibleKey1"), getAbsoluteKey("visibleKey2.subKey"),
      getAbsoluteKey("notVisibleKey"), "otherRootKey", "otherRootKey." + ROOT_KEY));
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("visibleKey1"))).thenReturn(true);
    when(mockVisibleKeyPredicate.apply(getAbsoluteKey("visibleKey2.subKey"))).thenReturn(true);
    assertEquals(ImmutableSet.of("visibleKey1", "visibleKey2.subKey"), dataStoreView.keySet());
    verify(mockVisibleKeyPredicate).apply(getAbsoluteKey("notVisibleKey"));
    verify(mockVisibleKeyPredicate, never()).apply("otherRootKey");
  }

  private String getAbsoluteKey(String key) {
    return ROOT_KEY + SimulationDataStore.KEY_SEPARATOR + key;
  }
}
