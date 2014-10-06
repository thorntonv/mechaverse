package org.mechaverse.simulation.common.genetic;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.mechaverse.simulation.common.SimulationDataStore;
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

  public static GeneticDataStore deserialize(byte[] data) throws IOException {
    return new GeneticDataStore(SimulationDataStore.deserialize(data));
  }

  public GeneticDataStore() {
    this(new SimulationDataStore());
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
    String suffix = "." + DATA_KEY;
    for (String key : dataStore.keySet()) {
      if (key.endsWith(suffix)) {
        keySet.add(key.substring(0, key.length() - suffix.length()));
      }
    }
    return keySet;
  }

  public GeneticData getCompositeData() {
    GeneticData.Builder geneticDataBuilder = GeneticData.newBuilder();
    int basePosition = 0;
    for (String key : keySet()) {
      GeneticData componentData = get(key);
      geneticDataBuilder.write(componentData.getData());

      for (int crossoverPoint : componentData.getCrossoverPoints()) {
        geneticDataBuilder.addCrossoverPoint(basePosition + crossoverPoint);
      }
      basePosition += componentData.getData().length;
    }
    return geneticDataBuilder.build();
  }

  public byte[] serialize() throws IOException {
    return dataStore.serialize();
  }

  private String geneticDataKey(String key) {
    return key + "." + DATA_KEY;
  }

  private String crossoverDataKey(String key) {
    return key + "." + CROSSOVER_DATA_KEY;
  }
}
