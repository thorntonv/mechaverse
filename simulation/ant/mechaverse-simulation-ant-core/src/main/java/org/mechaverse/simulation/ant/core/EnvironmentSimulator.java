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
import org.mechaverse.simulation.ant.api.util.EntityUtil;
import org.mechaverse.simulation.ant.core.module.AntReproductionModule;
import org.mechaverse.simulation.ant.core.module.FoodGenerationModule;
import org.mechaverse.simulation.ant.core.module.PheromoneDecayModule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Simulates an environment.
 */
public class EnvironmentSimulator implements EntityManager {

  private final CellEnvironment environment;
  private final Map<Entity, ActiveEntity> activeEntities = new LinkedHashMap<>();
  private final List<AntSimulationModule> modules = new ArrayList<>();
  private final ActiveEntityProvider[] activeEntityProviders;
  private final Set<EntityManager.Observer> observers = Sets.newLinkedHashSet();

  public EnvironmentSimulator(Environment environment,
      ActiveEntityProvider[] activeEntityProviders) {
    this(environment, ImmutableList.<AntSimulationModule>of(
      new FoodGenerationModule(), new AntReproductionModule(), new PheromoneDecayModule()),
      activeEntityProviders);
  }

  public EnvironmentSimulator(Environment environment, List<AntSimulationModule> modules,
      ActiveEntityProvider[] activeEntityProviders) {
    this.environment = new CellEnvironment(environment);
    this.activeEntityProviders = activeEntityProviders;

    for (Entity entity : environment.getEntities()) {
      addEntity(entity);
    }

    this.modules.addAll(modules);
    for(AntSimulationModule module : modules) {
      addObserver(module);
    }
  }

  public void update(AntSimulationState state, RandomGenerator random) {
    for (AntSimulationModule module : modules) {
      module.beforeUpdate(state, environment, this, random);
    }

    // Create a copy of the active entities because entities may be added or removed during the
    // update.
    List<ActiveEntity> activeEntityList = new ArrayList<>(activeEntities.values());

    for (ActiveEntity activeEntity : activeEntityList) {
      activeEntity.updateInput(environment, random);
    }

    for (AntSimulationModule module : modules) {
      module.beforePerformAction(state, environment, this, random);
    }

    for (ActiveEntity activeEntity : activeEntityList) {
      activeEntity.performAction(environment, state.getConfig(), this, random);
    }

    for (AntSimulationModule module : modules) {
      module.afterUpdate(state, environment, this, random);
    }
  }

  public void updateModel() {
    environment.updateModel();
  }

  @Override
  public void addEntity(Entity entity) {
    ActiveEntityProvider activeEntityProvider =
        activeEntityProviders[EntityUtil.getType(entity).ordinal()];
    if (activeEntityProvider != null) {
      ActiveEntity activeEntity = activeEntityProvider.getActiveEntity(entity);
      activeEntities.put(activeEntity.getEntity(), activeEntity);
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

  public Iterable<ActiveEntity> getActiveEntities() {
    return Iterables.unmodifiableIterable(activeEntities.values());
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
