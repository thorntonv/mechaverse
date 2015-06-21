package org.mechaverse.simulation.common.cellautomaton.genetic;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.genetic.GeneticData.Builder;
import org.mechaverse.simulation.common.util.ArrayUtil;

/**
 * Generates cellular automaton genetic data.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CellularAutomatonGeneticDataGenerator {

  // TODO(thorntonv): Implement unit test for this class.
  // TODO(thorntonv): Explore other crossover strategies.

  public static final String CELLULAR_AUTOMATON_STATE_KEY = "cellularAutomatonState";
  public static final String OUTPUT_MAP_KEY = "outputMap";

  public void generateGeneticData(GeneticDataStore dataStore, 
      CellularAutomatonSimulationModel model, int outputSize, RandomGenerator random) {
    dataStore.put(CELLULAR_AUTOMATON_STATE_KEY, generateStateGeneticData(model, random));
    dataStore.put(OUTPUT_MAP_KEY, generateOutputMapGeneticData(model, outputSize, random));
  }

  public GeneticData generateStateGeneticData(
      CellularAutomatonSimulationModel model, RandomGenerator random) {
    GeneticData.Builder geneticDataBuilder = GeneticData.newBuilder();
    for (int idx = 0; idx < model.getStateSize(); idx++) {
      geneticDataBuilder.writeInt(random.nextInt());
      geneticDataBuilder.markCrossoverPoint();
    }
    return geneticDataBuilder.build();
  }

  public GeneticData generateOutputMapGeneticData(CellularAutomatonSimulationModel model,
      int outputSize, RandomGenerator random) {
    return generateOutputMapGeneticData(
        outputSize, model.getCellOutputStateSize(), random);
  }

  public GeneticData generateOutputMapGeneticData(int outputSize, int stateSize,
      RandomGenerator random) {
    GeneticData.Builder geneticDataBuilder = GeneticData.newBuilder();
    for (int idx = 0; idx < outputSize; idx++) {
      geneticDataBuilder.writeInt(random.nextInt(stateSize));
      geneticDataBuilder.markCrossoverPoint();
    }
    return geneticDataBuilder.build();
  }

  public int[] getCellularAutomatonState(GeneticDataStore dataStore) {
    return ArrayUtil.toIntArray(dataStore.get(CELLULAR_AUTOMATON_STATE_KEY).getData());
  }

  public int[] getOutputMap(GeneticDataStore dataStore) {
    return ArrayUtil.toIntArray(dataStore.get(OUTPUT_MAP_KEY).getData());
  }
}
