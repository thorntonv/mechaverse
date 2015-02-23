package org.mechaverse.simulation.common.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.AbstractEntity;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.SimulationLogger;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.genetic.CellularAutomatonGeneticDataGenerator;
import org.mechaverse.simulation.common.genetic.CutAndSpliceCrossoverGeneticRecombinator;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.genetic.GeneticRecombinator;
import org.mechaverse.simulation.common.util.ArrayUtil;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

/**
 * A base class for simple simulations.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 * 
 * @param <E> the entity type
 * @param <M> the model type
 */
public class SimpleSimulation<E extends AbstractEntity, M> implements Simulation {

  public static final String AUTOMATON_STATE_KEY = "cellularAutomatonState";
  public static final String AUTOMATON_OUTPUT_MAP_KEY = "cellularAutomatonOutputMap";

  private final CellularAutomatonSimulationModel cellularAutomatonModel;
  private final CellularAutomatonSimulator simulator;
  private final Supplier<E> entitySupplier;
  private final Function<E, Double> entityFitnessFunction;
  private final GeneticRecombinator geneticRecombinator;

  private final CellularAutomatonGeneticDataGenerator geneticDataGenerator =
      new CellularAutomatonGeneticDataGenerator();

  private final List<E> entities = new LinkedList<>();
  private final Map<E, Integer> entityIndexMap = new IdentityHashMap<>();
  private final RandomGenerator random = new Well19937c();
  private final SimpleSimulationState<M> state;
  
  private final SimulationLogger<E, M> simulationLogger;
  
  public SimpleSimulation(SimpleSimulationState<M> state, Supplier<E> entitySupplier, 
      Function<E, Double> entityFitnessFunction, int populationSize, int inputSize, int outputSize,
      CellularAutomatonDescriptorDataSource descriptorDataSource, 
      SimulationLogger<E, M> simulationLogger) {
    this(state, entitySupplier, entityFitnessFunction, populationSize, inputSize, outputSize,
        descriptorDataSource.getDescriptor(), simulationLogger);
  }

  public SimpleSimulation(SimpleSimulationState<M> state, Supplier<E> entitySupplier, 
      Function<E, Double> entityFitnessFunction, int populationSize, int inputSize, int outputSize, 
          CellularAutomatonDescriptor descriptor, SimulationLogger<E, M> simulationLogger) {
    this(state, entitySupplier, entityFitnessFunction, 
        CellularAutomatonSimulationModelBuilder.build(descriptor), 
        new OpenClCellularAutomatonSimulator(populationSize, inputSize, outputSize, descriptor), 
        new CutAndSpliceCrossoverGeneticRecombinator(), simulationLogger);
  }

  public SimpleSimulation(SimpleSimulationState<M> state, Supplier<E> entitySupplier, 
      Function<E, Double> entityFitnessFunction, 
      CellularAutomatonSimulationModel cellularAutomatonModel,
      CellularAutomatonSimulator simulator, SimulationLogger<E, M> simulationLogger) {
    this(state, entitySupplier, entityFitnessFunction, cellularAutomatonModel, simulator, 
        new CutAndSpliceCrossoverGeneticRecombinator(), simulationLogger);
  }

  public SimpleSimulation(SimpleSimulationState<M> state, 
      Supplier<E> entitySupplier, 
      Function<E, Double> entityFitnessFunction, 
      CellularAutomatonSimulationModel cellularAutomatonModel, 
      CellularAutomatonSimulator simulator, 
      GeneticRecombinator geneticRecombinator,
      SimulationLogger<E, M> simulationLogger) {
    this.cellularAutomatonModel = cellularAutomatonModel;
    this.simulator = simulator;
    this.entitySupplier = entitySupplier;
    this.entityFitnessFunction = entityFitnessFunction;
    this.geneticRecombinator = geneticRecombinator;
    this.state = state;
    this.simulationLogger = simulationLogger;
    
    // Start with a random initial population.
    while (simulator.getAllocator().getAvailableCount() > 0) {
      generateRandomEntity();
    }
  }
  
  @Override
  public SimulationDataStore getState() throws Exception {
    return state;
  }

  @Override
  public void setState(SimulationDataStore stateDataStore) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public SimulationDataStore generateRandomState() throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void step(int stepCount) throws Exception {
    for (int cnt = 1; cnt <= stepCount; cnt++) {
      step();
    }
  }
  
  public void step() throws Exception {
    if (simulator.getAllocator().getAvailableCount() > 0) {
      EnumeratedDistribution<E> fitnessDistribution = null;
      if (fitnessDistribution == null) {
        fitnessDistribution = getFitnessDistribution();
      }
      generateEntity(fitnessDistribution);
    }

    for (E entity : entities) {
      simulator.setAutomatonInput(entityIndexMap.get(entity), entity.getInput());
    }

    simulator.update();

    int[] output = new int[simulator.getAutomatonOutputSize()];
    Iterator<E> entityIt = entities.iterator();
    while(entityIt.hasNext()) {
      E entity = entityIt.next();
      int entityIndex = entityIndexMap.get(entity);
      simulator.getAutomatonOutput(entityIndex, output);
      entity.processOutput(output);

      if (!entity.isAlive()) {
        simulator.getAllocator().deallocate(entityIndex);
        entityIt.remove();
        entityIndexMap.remove(entity);
        state.getEntityDataStore(entity).clear();
        state.getEntityGeneticDataStore(entity).clear();
      }
    }
    
    state.setIteration(state.getIteration() + 1);
    
    simulationLogger.log(state.getIteration(), state.getModel(), entities);
  }

  @Override
  public String getDeviceInfo() {
    return simulator.toString();
  }

  public List<E> getEntities() {
    return Collections.unmodifiableList(entities);
  }
  
  private E generateRandomEntity() {
    E entity = entitySupplier.get();
    entities.add(entity);
    entityIndexMap.put(entity, simulator.getAllocator().allocate());
    generateGeneticData(entity);
    initializeCellularAutomaton(entity);
    return entity;
  }
  
  private E generateEntity(EnumeratedDistribution<E> fitnessDistribution) {
    if (fitnessDistribution == null || fitnessDistribution.getPmf().size() < 2) {
      return generateRandomEntity();
    }

    E entity = entitySupplier.get();
    entities.add(entity);
    entityIndexMap.put(entity, simulator.getAllocator().allocate());

    E parent1 = fitnessDistribution.sample();
    E parent2 = fitnessDistribution.sample();

    // Ensure that parent1 != parent2.
    while (entities.size() > 1 && parent2 == parent1) {
      parent2 = fitnessDistribution.sample();
    }

    // Get the parents genetic information.
    GeneticDataStore parent1GeneticDataStore = state.getEntityGeneticDataStore(parent1);
    GeneticDataStore parent2GeneticDataStore = state.getEntityGeneticDataStore(parent2);

    GeneticDataStore childGeneticDataStore = state.getEntityGeneticDataStore(entity);
    for (String key : parent1GeneticDataStore.keySet()) {
      GeneticData parent1GeneticData = parent1GeneticDataStore.get(key);
      GeneticData parent2GeneticData = parent2GeneticDataStore.get(key);
      GeneticData childData = geneticRecombinator.recombine(
          parent1GeneticData, parent2GeneticData, random);
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
  }

  private IdentityHashMap<E, Double> getEntityFitnessMap() {
    IdentityHashMap<E, Double> entityFitnessMap = new IdentityHashMap<>(entities.size());
    for (E entity : entities) {
      if (entity.isAlive()) {
        double fitness = entityFitnessFunction.apply(entity);
        if (fitness > 0) {
          entityFitnessMap.put(entity, fitness);
        }
      }
    }
    return entityFitnessMap;
  }

  private EnumeratedDistribution<E> getFitnessDistribution() {
    return getFitnessDistribution(getEntityFitnessMap());
  }
  
  private EnumeratedDistribution<E> getFitnessDistribution(
      IdentityHashMap<E, Double> entityFitnessMap) {
    if (entities.isEmpty() || entityFitnessMap.isEmpty()) {
      return null;
    }
    
    double fitnessSum = 0;
    for (Double fitness : entityFitnessMap.values()) {
      fitnessSum += fitness;
    }
    
    List<Pair<E, Double>> pmf = new ArrayList<>();
    for (Map.Entry<E, Double> entry : entityFitnessMap.entrySet()) {
      E entity = entry.getKey();
      if (fitnessSum != 0) {
        pmf.add(new Pair<E, Double>(entity, entry.getValue()));
      } else {
        pmf.add(new Pair<E, Double>(entity, 1.0D / entities.size()));
      }
    }

    return new EnumeratedDistribution<E>(random, pmf);
  }
}
