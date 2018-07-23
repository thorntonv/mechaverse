package org.mechaverse.simulation.experimental.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticRecombinator;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonMutator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.genetic.GeneticRecombinator;
import org.mechaverse.simulation.common.genetic.selection.SelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.SelectionUtil;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.ArrayUtil;
import com.google.common.collect.Sets;
import gnu.trove.map.TObjectDoubleMap;

import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_OUTPUT_MAP_GENETIC_DATA_KEY;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY;

/**
 * A base class for simple simulations.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 *
 * @param <E> the entity type
 */
@SuppressWarnings("WeakerAccess")
public class SimpleSimulation<E extends SimpleCellularAutomatonEntity> implements Simulation {

  private final CellularAutomatonSimulationModel cellularAutomatonModel;
  private final CellularAutomatonSimulator simulator;
  private final Supplier<E> entitySupplier;
  private final Function<E, Double> entityFitnessFunction;
  private final SelectionStrategy<E> selectionStrategy;
  private final GeneticRecombinator geneticRecombinator;
  private final GeneticRecombinator cellularAutomatonGeneticRecombinator;

  private final int updatesPerIteration;

  private final CellularAutomatonGeneticDataGenerator geneticDataGenerator =
      new CellularAutomatonGeneticDataGenerator();

  private final List<E> entities = new LinkedList<>();
  private final Map<E, Integer> entityIndexMap = new IdentityHashMap<>();
  private final RandomGenerator random = new Well19937c();
  private final SimulationModel state;
  private final SimpleSimulationLogger<E, SimulationModel> simulationLogger;

  public SimpleSimulation(SimulationModel state, SimpleSimulationConfig<E, SimulationModel> config) {
    this.state = state;
    this.cellularAutomatonModel = config.getCellularAutomatonModel();
    this.simulator = config.getSimulator();
    this.entitySupplier = config.getEntitySupplier();
    this.entityFitnessFunction = config.getEntityFitnessFunction();
    this.selectionStrategy = config.getSelectionStrategy();
    this.geneticRecombinator = config.getGeneticRecombinator();
    this.cellularAutomatonGeneticRecombinator = new CellularAutomatonGeneticRecombinator(
      geneticRecombinator, new CellularAutomatonMutator(.001), cellularAutomatonModel);

    this.updatesPerIteration = config.getUpdatesPerIteration();
    this.simulationLogger = config.getSimulationLogger();
  }

  @Override
  public SimulationModel getState() {
    return state;
  }

  @Override
  public void setState(SimulationModel model) {
      throw new UnsupportedOperationException();
    }

  @Override
  public SimulationModel generateRandomState() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void step(int stepCount) {
    for (int cnt = 1; cnt <= stepCount; cnt++) {
      step();
    }
  }

  public void step(int stepCount, double targetFitness) {
    for (int cnt = 1; cnt <= stepCount; cnt++) {
      step();
      if (simulationLogger.getMinimize()) {
        if (simulationLogger.getOverallBestEntity().getValue() <= targetFitness) {
          return;
        }
      } else if (simulationLogger.getOverallBestEntity().getValue() >= targetFitness) {
        return;
      }
    }
  }

  public void step() {
    while (simulator.getAllocator().getAvailableCount() > 0) {
      generateRandomEntity();
    }

    for(int cnt = 1; cnt <= updatesPerIteration; cnt++) {
      for (E entity : entities) {
        simulator.setAutomatonInput(entityIndexMap.get(entity), entity.getInput());
      }

      simulator.update();
      int[] output = new int[simulator.getAutomatonOutputSize()];

      for(E entity : entities) {
        simulator.getAutomatonOutput(entityIndexMap.get(entity), output);
        entity.processOutput(output);
      }
    }

    simulationLogger.log(state.getIteration(), state, entities);

    TObjectDoubleMap<E> entityFitnessMap =
        SelectionUtil.buildEntityFitnessMap(entities, entityFitnessFunction);
    List<Pair<E, E>> selectedPairs =
        selectionStrategy.selectEntities(entityFitnessMap, entities.size(), random);

    List<E> previousGeneration = new ArrayList<>(entities);
    for(E entity : entities) {
      simulator.getAllocator().deallocate(entityIndexMap.get(entity));
    }
    entities.clear();
    entityIndexMap.clear();

    for (Pair<E, E> pair : selectedPairs) {
      generateEntity(pair);
    }

    for(E entity : previousGeneration) {
      entity.clearData();
      entity.getGeneticDataStore().clear();
    }

    state.setIteration(state.getIteration() + 1);
  }

  public List<E> getEntities() {
    return Collections.unmodifiableList(entities);
  }

  public SimpleSimulationLogger<E, SimulationModel> getLogger() {
    return simulationLogger;
  }

  private E generateRandomEntity() {
    E entity = entitySupplier.get();
    entities.add(entity);
    entityIndexMap.put(entity, simulator.getAllocator().allocate());
    generateGeneticData(entity);
    initializeCellularAutomaton(entity);
    return entity;
  }

  private E generateEntity(Pair<E, E> parents) {
    E entity = entitySupplier.get();
    entities.add(entity);
    entityIndexMap.put(entity, simulator.getAllocator().allocate());

    E parent1 = parents.getFirst();
    E parent2 = parents.getSecond();

    // Get the parents genetic information.
    GeneticDataStore parent1GeneticDataStore = parent1.getGeneticDataStore();
    GeneticDataStore parent2GeneticDataStore =
        (parent2 != null) ? parent2.getGeneticDataStore() : null;

    GeneticDataStore childGeneticDataStore = entity.getGeneticDataStore();
    for (String key : Sets.newHashSet(CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY,
            CELLULAR_AUTOMATON_OUTPUT_MAP_GENETIC_DATA_KEY)) {
      GeneticData parent1GeneticData = parent1GeneticDataStore.get(key);
      GeneticData childData = parent1GeneticData;
      if (parent2 != null) {
        GeneticData parent2GeneticData = parent2GeneticDataStore.get(key);
        if(CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY.equals(key)) {
          childData = cellularAutomatonGeneticRecombinator.recombine(
              parent1GeneticData, parent2GeneticData, random);
        } else {
          childData = geneticRecombinator.recombine(parent1GeneticData, parent2GeneticData, random);
        }
      }
      childGeneticDataStore.put(key, childData);
    }

    initializeCellularAutomaton(entity);

    return entity;
  }

  private void generateGeneticData(E entity) {
    GeneticDataStore geneticDataStore = new GeneticDataStore(entity);
    geneticDataGenerator.generateGeneticData(geneticDataStore, cellularAutomatonModel,
        simulator.getAutomatonOutputSize(), random);
  }

  private void initializeCellularAutomaton(E entity) {
    GeneticDataStore geneticDataStore = entity.getGeneticDataStore();
    int automatonIndex = entityIndexMap.get(entity);

    // Cellular automaton state.
    int[] automatonState = geneticDataGenerator.getCellularAutomatonState(geneticDataStore);
    simulator.setAutomatonState(automatonIndex, automatonState);
    entity.putData(CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY, ArrayUtil.toByteArray(automatonState));

    // Cellular automaton output map.
    int[] outputMap = geneticDataGenerator.getOutputMap(geneticDataStore);
    simulator.setAutomatonOutputMap(automatonIndex, outputMap);
    entity.putData(CELLULAR_AUTOMATON_OUTPUT_MAP_GENETIC_DATA_KEY, ArrayUtil.toByteArray(outputMap));

    entity.setCellularAutomatonModel(cellularAutomatonModel);
    entity.setGeneticDataStore(geneticDataStore);
    entity.setCellularAutomaton(new SimulatorCellularAutomaton(
        cellularAutomatonModel, simulator, automatonIndex));
  }
}
