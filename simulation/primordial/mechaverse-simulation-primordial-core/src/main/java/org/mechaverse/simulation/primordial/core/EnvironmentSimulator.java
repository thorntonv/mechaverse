package org.mechaverse.simulation.primordial.core;

import java.util.*;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.primordial.core.entity.ActiveEntity;
import org.mechaverse.simulation.primordial.core.entity.ActiveEntityProviders;
import org.mechaverse.simulation.primordial.core.entity.ActiveEntityProvider;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.SimulationModelUtil;
import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.common.model.Environment;
import org.mechaverse.simulation.primordial.core.module.PrimordialSimulationModule;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Simulates an environment.
 */
public final class EnvironmentSimulator implements EntityManager<SimulationModel, PrimordialSimulationState, EntityType, Cell, CellEnvironment, ActiveEntity>, AutoCloseable {

  public static class Factory {

    @Autowired
    ApplicationContext context;
    @Autowired
    private ActiveEntityProviders activeEntityProviders;
    @Autowired
    private ObjectFactory<List<PrimordialSimulationModule>> modulesFactory;

    public EnvironmentSimulator create(String environmentId) {
      EnvironmentSimulator environmentSimulator =
          new EnvironmentSimulator(environmentId, activeEntityProviders, modulesFactory.getObject());
      context.getAutowireCapableBeanFactory().autowireBean(environmentSimulator);
      return environmentSimulator;
    }
  }


  private final String environmentId;
  private CellEnvironment environment;
  private final Map<Entity, ActiveEntity> activeEntities = new LinkedHashMap<>();
  private final Set<EntityManager.Observer> observers = Sets.newLinkedHashSet();
  private final ActiveEntityProviders activeEntityProviders;
  private final List<PrimordialSimulationModule> modules;

  private PrimordialSimulationState state;

  private EnvironmentSimulator(String environmentId, ActiveEntityProviders activeEntityProviders, List<PrimordialSimulationModule> modules) {
    this.environmentId = environmentId;
    this.activeEntityProviders = activeEntityProviders;
    this.modules = modules;
  }

  public void update(PrimordialSimulationState state, RandomGenerator random) {
    this.state = state;

    for (PrimordialSimulationModule module : modules) {
      module.beforeUpdate(state, environment, this, random);
    }

    // Create a copy of the active entities because entities may be added or removed during the
    // update.
    List<ActiveEntity> activeEntityList = new ArrayList<>(activeEntities.values());

    for (ActiveEntity activeEntity : activeEntityList) {
      activeEntity.updateInput(environment, random);
    }

    for (PrimordialSimulationModule module : modules) {
      module.beforePerformAction(state, environment, this, random);
    }

    for (ActiveEntity activeEntity : activeEntityList) {
      activeEntity.performAction(environment, this, random);
    }

    for (PrimordialSimulationModule module : modules) {
      module.afterUpdate(state, environment, this, random);
    }
  }

  public void setState(PrimordialSimulationState state) {
    cleanUp();

    this.state = state;

    for (PrimordialSimulationModule module : modules) {
      module.setState(state, environment, this);
    }

    Environment environmentModel =
        SimulationModelUtil.getEnvironment(state.getModel(), environmentId);
    this.environment = new CellEnvironment(environmentModel);
    List<Entity> entities = new ArrayList<>(environmentModel.getEntities());
    for (Entity entity : entities) {
      addEntity(entity);
    }

    for (ActiveEntity activeEntity : getActiveEntities()) {
      activeEntity.setState(state);
    }

    for (PrimordialSimulationModule module : modules) {
      addObserver(module);
    }
  }

  public void updateState(PrimordialSimulationState state) {
    for (PrimordialSimulationModule module : modules) {
      module.updateState(state, environment, this);
    }
    for (ActiveEntity activeEntity : getActiveEntities()) {
      activeEntity.updateState(state);
    }
    environment.updateModel();
  }

  @Override
  public void addEntity(Entity entity) {
    ActiveEntityProvider activeEntityProvider = activeEntityProviders.get(entity);
    if (activeEntityProvider != null) {
      ActiveEntity activeEntity = activeEntityProvider.getActiveEntity(entity);
      if (state != null) {
        activeEntity.setState(state);
      }
      activeEntities.put(activeEntity.getEntity(), activeEntity);
    }
    for (EntityManager.Observer observer : observers) {
      observer.onAddEntity(entity, state);
    }
  }

  @Override
  public void removeEntity(Entity entity) {
    activeEntities.remove(entity);
    environment.getCell(entity).removeEntity(entity);
    for (EntityManager.Observer observer : observers) {
      observer.onRemoveEntity(entity, state);
    }
  }

  @Override
  public void removeEntity(ActiveEntity activeEntity) {
    activeEntities.remove(activeEntity.getEntity());
    environment.getCell(activeEntity.getEntity()).removeEntity(activeEntity.getType());
    activeEntity.updateState(state);
    for (EntityManager.Observer observer : observers) {
      observer.onRemoveEntity(activeEntity.getEntity(), state);
    }
  }

  public Iterable<ActiveEntity> getActiveEntities() {
    return Iterables.unmodifiableIterable(activeEntities.values());
  }

  @Override
  public void addObserver(Observer observer) {
    observers.add(observer);

    for (Entity entity : environment.getEnvironment().getEntities()) {
      observer.onAddEntity(entity, state);
    }
  }

  @Override
  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }

  public CellEnvironment getEnvironment() {
    return environment;
  }

  @Override
  public void close() {
    cleanUp();
  }

  private void cleanUp() {
    observers.removeAll(modules);

    // Clean up the active entities.
    for (ActiveEntity entity : activeEntities.values()) {
      entity.onRemoveEntity();
    }
    activeEntities.clear();
  }
}
