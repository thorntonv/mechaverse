package org.mechaverse.simulation.common.genetic;

import java.util.LinkedHashSet;
import java.util.Set;

import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.util.ArrayUtil;

/**
 * A {@link SimulationDataStore} that stores {@link GeneticData}.
 *
 * @author Vance Thornton <thorntonv@mechaverse.org>
 */
public class GeneticDataStore {

  // TODO(thorntonv): Implement unit test for this class.

  public static final String KEY = "geneticData";

  private static final String DATA_KEY = "data";
  private static final String CROSSOVER_DATA_KEY = "crossoverData";

  private final SimulationDataStore dataStore;

  public GeneticDataStore() {
    this(new MemorySimulationDataStore());
  }

  public GeneticDataStore(SimulationDataStore dataStore) {
    this.dataStore = dataStore;
  }

  public void put(String key, GeneticData geneticData) {
    dataStore.put(geneticDataKey(key), geneticData.getData());
    dataStore.put(crossoverDataKey(key), ArrayUtil.toByteArray(geneticData.getCrossoverPoints()));
  }

  public GeneticData get(String key) {
    byte[] data = dataStore.get(geneticDataKey(key));
    int[] crossoverPoints = ArrayUtil.toIntArray(dataStore.get(crossoverDataKey(key)));
    return new GeneticData(data, crossoverPoints);
  }

  public void remove(String key) {
    dataStore.remove(geneticDataKey(key));
    dataStore.remove(crossoverDataKey(key));
  }

  public Set<String> keySet() {
    Set<String> keySet = new LinkedHashSet<>();
    String suffix = SimulationDataStore.KEY_SEPARATOR + DATA_KEY;
    for (String key : dataStore.keySet()) {
      if (key.endsWith(suffix)) {
        keySet.add(key.substring(0, key.length() - suffix.length()));
      }
    }
    return keySet;
  }

  public void clear() {
    dataStore.clear();
  }

  public int size() {
    return keySet().size();
  }

  private String geneticDataKey(String key) {
    return key + SimulationDataStore.KEY_SEPARATOR + DATA_KEY;
  }

  private String crossoverDataKey(String key) {
    return key + SimulationDataStore.KEY_SEPARATOR + CROSSOVER_DATA_KEY;
  }
}
