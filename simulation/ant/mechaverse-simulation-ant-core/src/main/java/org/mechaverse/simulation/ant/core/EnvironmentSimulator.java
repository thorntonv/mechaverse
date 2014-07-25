package org.mechaverse.simulation.ant.core;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.Environment;

import com.google.common.base.Optional;

/**
 * Simulates an environment.
 */
public class EnvironmentSimulator implements EntityManager {

  private final CellEnvironment environment;
  private final Map<Entity, ActiveEntity> activeEntities = new IdentityHashMap<>();
  private final ActiveEntityProvider activeEntityProvider;

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
    environment.updateModel();
  }

  @Override
  public void addEntity(Entity entity) {
    Optional<ActiveEntity> activeEntity = activeEntityProvider.getActiveEntity(entity);
    if (activeEntity.isPresent()) {
      activeEntities.put(activeEntity.get().getEntity(), activeEntity.get());
    }
  }

  @Override
  public void removeEntity(Entity entity) {
    activeEntities.remove(entity);
    environment.getCell(entity).removeEntity(entity);
  }

  @Override
  public void removeEntity(ActiveEntity activeEntity) {
    activeEntities.remove(activeEntity.getEntity());
    environment.getCell(activeEntity.getEntity()).removeEntity(activeEntity.getType());
  }
}
