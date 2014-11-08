package org.mechaverse.simulation.common.genetic;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.simulation.common.util.ArrayUtil;

/**
 * Generates circuit genetic data.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CircuitGeneticDataGenerator {

  // TODO(thorntonv): Implement unit test for this class.
  // TODO(thorntonv): Explore other crossover strategies.

  public static final String CIRCUIT_STATE_KEY = "circuitState";
  public static final String OUTPUT_MAP_KEY = "outputMap";

  public void generateGeneticData(GeneticDataStore dataStore, CircuitSimulationModel circuitModel,
      int outputSize,RandomGenerator random) {
    dataStore.put(CIRCUIT_STATE_KEY, generateStateGeneticData(circuitModel, random));
    dataStore.put(OUTPUT_MAP_KEY, generateOutputMapGeneticData(circuitModel, outputSize, random));
  }

  public GeneticData generateStateGeneticData(
      CircuitSimulationModel circuitModel, RandomGenerator random) {
    GeneticData.Builder geneticDataBuilder = GeneticData.newBuilder();
    for (int idx = 0; idx < circuitModel.getCircuitStateSize(); idx++) {
      geneticDataBuilder.writeInt(random.nextInt());
      geneticDataBuilder.markCrossoverPoint();
    }
    return geneticDataBuilder.build();
  }

  public GeneticData generateOutputMapGeneticData(CircuitSimulationModel circuitModel,
      int outputSize, RandomGenerator random) {
    int elementOutputStateSize = 0;
    for(ElementInfo elementInfo : circuitModel.getLogicalUnitInfo().getElements()) {
      elementOutputStateSize += elementInfo.getOutputVarNames().size();
    }
    elementOutputStateSize *= circuitModel.getLogicalUnitCount();
    return generateOutputMapGeneticData(outputSize, elementOutputStateSize, random);
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

  public int[] getCircuitState(GeneticDataStore dataStore) {
    return ArrayUtil.toIntArray(dataStore.get(CIRCUIT_STATE_KEY).getData());
  }

  public int[] getOutputMap(GeneticDataStore dataStore) {
    return ArrayUtil.toIntArray(dataStore.get(OUTPUT_MAP_KEY).getData());
  }
}
