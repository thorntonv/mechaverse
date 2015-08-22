package org.mechaverse.simulation.common.datastore;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Provides a relative and filtered view of a {@link SimulationDataStore}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class SimulationDataStoreView implements SimulationDataStore {

  private final String rootKeyPrefix;
  private final Predicate<String> visibleKeyPredicate;
  private final SimulationDataStore dataStore;

  /**
   * Constructs a new view of the given {@link SimulationDataStore}.
   *
   * @param visibleKeyPredicate a predicate that applies to keys that are visible in this view
   * @param dataStore the {@link SimulationDataStore} this is a view of
   */
  public SimulationDataStoreView(Predicate<String> visibleKeyPredicate,
      SimulationDataStore dataStore) {
    this("", visibleKeyPredicate, dataStore);
  }

  /**
   * Constructs a new view of the given {@link SimulationDataStore}.
   *
   * @param rootKey the root key that keys for this view will be relative to
   * @param dataStore the {@link SimulationDataStore} this is a view of
   */
  public SimulationDataStoreView(String rootKey, SimulationDataStore dataStore) {
    this(rootKey, Predicates.<String>alwaysTrue(), dataStore);
  }

  /**
   * Constructs a new view of the given {@link SimulationDataStore}.
   *
   * @param rootKey the root key that keys for this view will be relative to
   * @param visibleKeyPredicate a predicate that applies to keys that are visible in this view
   * @param dataStore the {@link SimulationDataStore} this is a view of
   */
  public SimulationDataStoreView(String rootKey, Predicate<String> visibleKeyPredicate,
      SimulationDataStore dataStore) {
    this.rootKeyPrefix = rootKey.length() > 0 ? rootKey + SimulationDataStore.KEY_SEPARATOR : "";
    this.visibleKeyPredicate = visibleKeyPredicate;
    this.dataStore = dataStore;
  }

  @Override
  public byte[] get(String key) {
    String absoluteKey = rootKeyPrefix + key;
    return visibleKeyPredicate.apply(absoluteKey) ? dataStore.get(absoluteKey) : null;
  }

  @Override
  public void put(String key, byte[] value) {
    String absoluteKey = rootKeyPrefix + key;
    if (visibleKeyPredicate.apply(absoluteKey)) {
      dataStore.put(absoluteKey, value);
    } else {
      throw new IllegalArgumentException("The key " + key + " is not visible in this view.");
    }
  }

  @Override
  public void merge(SimulationDataStore fromDataStore) {
    dataStore.merge(fromDataStore);
  }

  @Override
  public void remove(String key) {
    String absoluteKey = rootKeyPrefix + key;
    if (visibleKeyPredicate.apply(absoluteKey)) {
      dataStore.remove(absoluteKey);
    } else {
      throw new IllegalArgumentException("The key " + key + " is not visible in this view.");
    }
  }

  @Override
  public void clear() {
    for (String key : keySet()) {
      remove(key);
    }
  }

  @Override
  public boolean containsKey(String key) {
    String absoluteKey = rootKeyPrefix + key;
    return visibleKeyPredicate.apply(absoluteKey) && dataStore.containsKey(absoluteKey);
  }

  @Override
  public Set<String> keySet() {
    Set<String> keys = new HashSet<>();
    for (String key : dataStore.keysWithPrefix(rootKeyPrefix)) {
      if (isVisible(key)) {
        keys.add(getRelativeKey(key));
      }
    }
    return keys;
  }

  @Override
  public int size() {
    return keySet().size();
  }

  @Override
  public boolean equals(Object otherObject) {
    return dataStore.equals(otherObject);
  }

  @Override
  public int hashCode() {
    return dataStore.hashCode();
  }

  @Override
  public Set<String> keysWithPrefix(String prefix) {
    Set<String> keys = new HashSet<>();
    for (String key : dataStore.keysWithPrefix(getAbsoluteKey(prefix))) {
      if (isVisible(key)) {
        keys.add(getRelativeKey(key));
      }
    }

    return keys;
  }

  private boolean isVisible(String absoluteKey) {
    return absoluteKey.startsWith(rootKeyPrefix) && visibleKeyPredicate.apply(absoluteKey);
  }

  private String getAbsoluteKey(String relativeKey) {
    return rootKeyPrefix + relativeKey;
  }

  private String getRelativeKey(String absoluteKey) {
    return absoluteKey.substring(rootKeyPrefix.length());
  }
}
