package org.mechaverse.simulation.ant.core;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.entity.ActiveEntity;
import org.mechaverse.simulation.ant.core.entity.ActiveEntityProviders;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveEntityProvider;
import org.mechaverse.simulation.ant.core.module.AntSimulationModule;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.SimulationModelUtil;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Simulates an environment.
 */
public final class EnvironmentSimulator
    implements EntityManager<SimulationModel, AntSimulationState>, AutoCloseable {

  public static class Factory {

    @Autowired ApplicationContext context;
    @Autowired private ActiveEntityProviders activeEntityProviders;
    @Autowired private ObjectFactory<List<AntSimulationModule>> modulesFactory;

    public EnvironmentSimulator create(String environmentId) {
      EnvironmentSimulator environmentSimulator =
          new EnvironmentSimulator(environmentId, activeEntityProviders, modulesFactory.getObject());
      context.getAutowireCapableBeanFactory().autowireBean(environmentSimulator);
      return environmentSimulator;
    }
  }


  private final String environmentId;
  private CellEnvironment environment;
  private final Map<EntityModel, ActiveEntity> activeEntities = new LinkedHashMap<>();
  private final Set<EntityManager.Observer<SimulationModel, AntSimulationState>> observers =
      Sets.newLinkedHashSet();
  private final ActiveEntityProviders activeEntityProviders;
  private final List<AntSimulationModule> modules;

  private AntSimulationState state;

  private EnvironmentSimulator(String environmentId, ActiveEntityProviders activeEntityProviders,
      List<AntSimulationModule> modules) {
    this.environmentId = environmentId;
    this.activeEntityProviders = activeEntityProviders;
    this.modules = modules;
  }

  public void update(AntSimulationState state, RandomGenerator random) {
    this.state = state;

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

  public void setState(AntSimulationState state) {
    cleanUp();

    this.state = state;

    for (AntSimulationModule module : modules) {
      module.setState(state, environment, this);
    }

    EnvironmentModel environmentModel = SimulationModelUtil.getEnvironment(
        state.getModel(), environmentId);
    this.environment = new CellEnvironment(environmentModel);
    List<EntityModel> entities = new ArrayList<>(environmentModel.getEntities());
    for (EntityModel entity : entities) {
      addEntity(entity);
    }

    for (ActiveEntity activeEntity : getActiveEntities()) {
      activeEntity.setState(state);
    }

    for(AntSimulationModule module : modules) {
      addObserver(module);
    }
  }

  public void updateState(AntSimulationState state) {
    for (AntSimulationModule module : modules) {
      module.updateState(state, environment, this);
    }
    for (ActiveEntity activeEntity : getActiveEntities()) {
      activeEntity.updateState(state);
    }
    environment.updateModel();
  }

  @Override
  public void addEntity(EntityModel entity) {
    ActiveEntityProvider activeEntityProvider = activeEntityProviders.get(entity);
    if (activeEntityProvider != null) {
      ActiveEntity activeEntity = activeEntityProvider.getActiveEntity(entity);
      if (state != null) {
        activeEntity.setState(state);
      }
      activeEntities.put(activeEntity.getEntity(), activeEntity);
    }
    for (EntityManager.Observer<SimulationModel, AntSimulationState> observer : observers) {
      observer.onAddEntity(entity, state);
    }
  }

  @Override
  public void removeEntity(EntityModel entity) {
    ActiveEntity activeEntity = activeEntities.remove(entity);
    environment.getCell(entity).removeEntity(entity);
    if(activeEntity != null) {
      activeEntity.updateState(state);
    }
    for (EntityManager.Observer<SimulationModel, AntSimulationState> observer : observers) {
      observer.onRemoveEntity(entity, state);
    }
  }

  public Iterable<ActiveEntity> getActiveEntities() {
    return Iterables.unmodifiableIterable(activeEntities.values());
  }

  @Override
  public void addObserver(Observer<SimulationModel, AntSimulationState> observer) {
    observers.add(observer);

    for (EntityModel entity : environment.getEnvironment().getEntities()) {
      observer.onAddEntity(entity, state);
    }
  }

  @Override
  public void removeObserver(Observer observer) {
    observers.remove(observer);
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
