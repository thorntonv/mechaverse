package org.mechaverse.simulation.common.datastore;

import java.io.IOException;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

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
  public byte[] get(String key) throws IOException {
    String absoluteKey = rootKeyPrefix + key;
    return visibleKeyPredicate.apply(absoluteKey) ? dataStore.get(absoluteKey) : null;
  }

  @Override
  public void put(String key, byte[] value) throws IOException {
    String absoluteKey = rootKeyPrefix + key;
    if (visibleKeyPredicate.apply(absoluteKey)) {
      dataStore.put(absoluteKey, value);
    } else {
      throw new IOException("The key " + key + " is not visible in this view.");
    }
  }

  @Override
  public void remove(String key) throws IOException {
    String absoluteKey = rootKeyPrefix + key;
    if (visibleKeyPredicate.apply(absoluteKey)) {
      dataStore.remove(absoluteKey);
    } else {
      throw new IOException("The key " + key + " is not visible in this view.");
    }
  }

  @Override
  public boolean containsKey(String key) throws IOException {
    String absoluteKey = rootKeyPrefix + key;
    return visibleKeyPredicate.apply(absoluteKey) ? dataStore.containsKey(absoluteKey) : false;
  }

  @Override
  public Set<String> keySet() throws IOException {
    ImmutableSet.Builder<String> keySetBuilder = ImmutableSet.builder();
    for (String absoluteKey : dataStore.keySet()) {
      if (absoluteKey.startsWith(rootKeyPrefix) && visibleKeyPredicate.apply(absoluteKey)) {
        String relativeKey = absoluteKey.substring(rootKeyPrefix.length());
        keySetBuilder.add(relativeKey);
      }
    }
    return keySetBuilder.build();
  }

  @Override
  public int size() throws IOException {
    return keySet().size();
  }
}
