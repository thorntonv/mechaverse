package org.mechaverse.simulation.ant.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.Environment;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

/**
 * Simulates an environment.
 */
public class EnvironmentSimulator implements EntityManager {

  private final CellEnvironment environment;
  private final Map<Entity, ActiveEntity> activeEntities = new LinkedHashMap<>();
  private final List<EnvironmentSimulationModule> modules = new ArrayList<>();
  private final ActiveEntityProvider activeEntityProvider;
  private final Set<EntityManager.Observer> observers = Sets.newLinkedHashSet();

  public EnvironmentSimulator(Environment environment, ActiveEntityProvider activeEntityProvider) {
    this(environment, ImmutableList.<EnvironmentSimulationModule>of(
      new FoodGenerator(), new AntReproductionModule(), new PheromoneDecayModule()),
      activeEntityProvider);
  }

  public EnvironmentSimulator(Environment environment, List<EnvironmentSimulationModule> modules,
      ActiveEntityProvider activeEntityProvider) {
    this.environment = new CellEnvironment(environment);
    this.activeEntityProvider = activeEntityProvider;

    for (Entity entity : environment.getEntities()) {
      addEntity(entity);
    }

    this.modules.addAll(modules);
    for(EnvironmentSimulationModule module : modules) {
      addObserver(module);
    }
  }

  public void update(AntSimulationState state, RandomGenerator random) {
    for (EnvironmentSimulationModule module : modules) {
      module.update(state, environment, this, random);
    }

    // Create a copy of the active entities because they may add or remove other entities.
    List<ActiveEntity> activeEntityList = new ArrayList<>(activeEntities.values());

    for (ActiveEntity activeEntity : activeEntityList) {
      activeEntity.updateInput(environment, random);
    }
    for (ActiveEntity activeEntity : activeEntityList) {
      activeEntity.performAction(environment, state.getConfig(), this, random);
    }
  }

  public void updateModel() {
    for (ActiveEntity activeEntity : activeEntities.values()) {
      activeEntity.updateModel();
    }
    environment.updateModel();
  }

  @Override
  public void addEntity(Entity entity) {
    Optional<ActiveEntity> activeEntity = activeEntityProvider.getActiveEntity(entity);
    if (activeEntity.isPresent()) {
      activeEntities.put(activeEntity.get().getEntity(), activeEntity.get());
    }
    for (EntityManager.Observer observer : observers) {
      observer.onAddEntity(entity);
    }
  }

  @Override
  public void removeEntity(Entity entity) {
    activeEntities.remove(entity);
    environment.getCell(entity).removeEntity(entity);
    for (EntityManager.Observer observer : observers) {
      observer.onRemoveEntity(entity);
    }
  }

  @Override
  public void removeEntity(ActiveEntity activeEntity) {
    activeEntities.remove(activeEntity.getEntity());
    environment.getCell(activeEntity.getEntity()).removeEntity(activeEntity.getType());
    for (EntityManager.Observer observer : observers) {
      observer.onRemoveEntity(activeEntity.getEntity());
    }
  }

  @Override
  public void addObserver(Observer observer) {
    observers.add(observer);

    for (Entity entity : environment.getEnvironment().getEntities()) {
      observer.onAddEntity(entity);
    }
  }

  @Override
  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }
}
