package org.mechaverse.simulation.ant.core;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.Environment;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

/**
 * Simulates an environment.
 */
public class EnvironmentSimulator implements EntityManager {

  private final CellEnvironment environment;
  private final Map<Entity, ActiveEntity> activeEntities = new IdentityHashMap<>();
  private final ActiveEntityProvider activeEntityProvider;
  private final Set<EntityManager.Observer> observers = Sets.newIdentityHashSet();

  public EnvironmentSimulator(Environment environment, ActiveEntityProvider activeEntityProvider) {
    this.environment = new CellEnvironment(environment);
    this.activeEntityProvider = activeEntityProvider;

    for (Entity entity : environment.getEntities()) {
      addEntity(entity);
    }
  }

  public void update(RandomGenerator random) {
    List<ActiveEntity> activeEntityList = new ArrayList<>(activeEntities.values());
    for (ActiveEntity activeEntity : activeEntityList) {
      activeEntity.updateInput(environment, random);
    }
    for (ActiveEntity activeEntity : activeEntityList) {
      activeEntity.performAction(environment, this, random);
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
  }

  @Override
  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }
}
