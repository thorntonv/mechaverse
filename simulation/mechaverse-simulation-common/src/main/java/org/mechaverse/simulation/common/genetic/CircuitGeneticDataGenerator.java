package org.mechaverse.simulation.common.genetic;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.util.ArrayUtil;

/**
 * Generates circuit genetic data.
 *
 * @author Vance Thornton <thorntonv@mechaverse.org>
 */
public class CircuitGeneticDataGenerator {

  // TODO(thorntonv): Implement unit test for this class.
  // TODO(thorntonv): Explore other crossover strategies.

  public static final String CIRCUIT_STATE_KEY = "circuitState";
  public static final String OUTPUT_MAP_KEY = "outputMap";

  public GeneticDataStore generateGeneticData(int stateSize, int outputSize,
      RandomGenerator random) {
    GeneticDataStore dataStore = new GeneticDataStore();
    dataStore.put(CIRCUIT_STATE_KEY, generateStateGeneticData(stateSize, random));
    dataStore.put(OUTPUT_MAP_KEY, generateOutputMapGeneticData(outputSize, stateSize, random));
    return dataStore;
  }

  public GeneticData generateStateGeneticData(int stateSize, RandomGenerator random) {
    GeneticData.Builder geneticDataBuilder = GeneticData.newBuilder();
    for (int idx = 0; idx < stateSize; idx++) {
      geneticDataBuilder.writeInt(random.nextInt());
      geneticDataBuilder.markCrossoverPoint();
    }
    return geneticDataBuilder.build();
  }

  public GeneticData generateOutputMapGeneticData(
      int outputSize, int stateSize, RandomGenerator random) {
    GeneticData.Builder geneticDataBuilder = GeneticData.newBuilder();
    for (int idx = 0; idx < outputSize; idx++) {
      geneticDataBuilder.writeInt(random.nextInt(stateSize));
      geneticDataBuilder.markCrossoverPoint();
    }
    return geneticDataBuilder.build();
  }

  public int[] getCircuitState(GeneticDataStore dataStore) {
    return ArrayUtil.toIntArray(dataStore.get(CIRCUIT_STATE_KEY).getData());
  }

  public int[] getOutputMap(GeneticDataStore dataStore) {
    return ArrayUtil.toIntArray(dataStore.get(OUTPUT_MAP_KEY).getData());
  }
}
