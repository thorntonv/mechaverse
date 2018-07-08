package org.mechaverse.simulation.experimental.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.genetic.GeneticRecombinator;
import org.mechaverse.simulation.common.genetic.selection.SelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.SelectionUtil;
import org.mechaverse.simulation.common.util.ArrayUtil;

import java.util.function.Function;
import java.util.function.Supplier;

import gnu.trove.map.TObjectDoubleMap;

/**
 * A base class for simple simulations.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 *
 * @param <E> the entity type
 * @param <M> the model type
 */
public class SimpleSimulation<E extends SimpleCellularAutomatonEntity, M> implements Simulation {

  public static final String AUTOMATON_STATE_KEY = "cellularAutomatonState";
  public static final String AUTOMATON_OUTPUT_MAP_KEY = "cellularAutomatonOutputMap";

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
  private final SimpleSimulationState<M> state;
  private final SimpleSimulationLogger<E, M> simulationLogger;

  public SimpleSimulation(SimpleSimulationState<M> state, SimpleSimulationConfig<E, M> config) {
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
  public SimulationDataStore getState() {
    return state;
  }

  @Override
  public void setState(SimulationDataStore stateDataStore) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SimulationDataStore generateRandomState() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void step(int stepCount) {
    for (int cnt = 1; cnt <= stepCount; cnt++) {
      step();
    }
  }

  @Override
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

    simulationLogger.log(state.getIteration(), state.getModel(), entities);

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
      state.getEntityDataStore(entity).clear();
      state.getEntityGeneticDataStore(entity).clear();
    }

    state.setIteration(state.getIteration() + 1);
  }

  @Override
  public String getDeviceInfo() {
    return simulator.toString();
  }

  public List<E> getEntities() {
    return Collections.unmodifiableList(entities);
  }

  public SimpleSimulationLogger<E, M> getLogger() {
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
    GeneticDataStore parent1GeneticDataStore = state.getEntityGeneticDataStore(parent1);
    GeneticDataStore parent2GeneticDataStore =
        (parent2 != null) ? state.getEntityGeneticDataStore(parent2) : null;

    GeneticDataStore childGeneticDataStore = state.getEntityGeneticDataStore(entity);
    for (String key : parent1GeneticDataStore.keySet()) {
      GeneticData parent1GeneticData = parent1GeneticDataStore.get(key);
      GeneticData childData = parent1GeneticData;
      if (parent2 != null) {
        GeneticData parent2GeneticData = parent2GeneticDataStore.get(key);
        if(AUTOMATON_STATE_KEY.equals(key)) {
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
    GeneticDataStore geneticDataStore = state.getEntityGeneticDataStore(entity);
    geneticDataGenerator.generateGeneticData(geneticDataStore, cellularAutomatonModel,
        simulator.getAutomatonOutputSize(), random);
  }

  private void initializeCellularAutomaton(E entity) {
    SimulationDataStore dataStore = state.getEntityDataStore(entity);
    GeneticDataStore geneticDataStore = state.getEntityGeneticDataStore(entity);
    int automatonIndex = entityIndexMap.get(entity);

    // Cellular automaton state.
    int[] automatonState = geneticDataGenerator.getCellularAutomatonState(geneticDataStore);
    simulator.setAutomatonState(automatonIndex, automatonState);
    dataStore.put(AUTOMATON_STATE_KEY, ArrayUtil.toByteArray(automatonState));

    // Cellular automaton output map.
    int[] outputMap = geneticDataGenerator.getOutputMap(geneticDataStore);
    simulator.setAutomatonOutputMap(automatonIndex, outputMap);
    dataStore.put(AUTOMATON_OUTPUT_MAP_KEY, ArrayUtil.toByteArray(outputMap));

    entity.setCellularAutomatonModel(cellularAutomatonModel);
    entity.setGeneticDataStore(geneticDataStore);
    entity.setCellularAutomaton(new SimulatorCellularAutomaton(
        cellularAutomatonModel, simulator, automatonIndex));
  }
}
