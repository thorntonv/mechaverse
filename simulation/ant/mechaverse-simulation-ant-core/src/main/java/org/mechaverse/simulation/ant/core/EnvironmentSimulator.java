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
import org.mechaverse.simulation.ant.core.module.AntSimulationModule;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Simulates an environment.
 */
public final class EnvironmentSimulator implements EntityManager {

  public static class Factory {

    @Autowired ApplicationContext context;
    @Autowired private ActiveEntityProviders activeEntityProviders;
    @Autowired private ObjectFactory<List<AntSimulationModule>> modulesFactory;

    public EnvironmentSimulator create(Environment environment) {
      EnvironmentSimulator environmentSimulator =
          new EnvironmentSimulator(environment, activeEntityProviders, modulesFactory.getObject());
      context.getAutowireCapableBeanFactory().autowireBean(environmentSimulator);
      return environmentSimulator;
    }
  }


  private final CellEnvironment environment;
  private final Map<Entity, ActiveEntity> activeEntities = new LinkedHashMap<>();
  private final Set<EntityManager.Observer> observers = Sets.newLinkedHashSet();
  private final ActiveEntityProviders activeEntityProviders;
  private final List<AntSimulationModule> modules;

  private EnvironmentSimulator(Environment environment, ActiveEntityProviders activeEntityProviders,
      List<AntSimulationModule> modules) {
    this.environment = new CellEnvironment(environment);
    this.activeEntityProviders =activeEntityProviders;
    this.modules = modules;

    for (Entity entity : environment.getEntities()) {
      addEntity(entity);
    }

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
      activeEntity.performAction(environment, this, random);
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
    ActiveEntityProvider activeEntityProvider = activeEntityProviders.get(entity);
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
