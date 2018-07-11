package org.mechaverse.simulation.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * Simulates an environment.
 */
public abstract class AbstractEnvironment<
    SIM_MODEL extends SimulationModel,
    ENV_MODEL extends EnvironmentModel,
    ENT_MODEL extends EntityModel,
    ENT_TYPE extends Enum<ENT_TYPE>,
    ENT extends Entity<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>>
    implements Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL>, EntityManager<SIM_MODEL, ENT_MODEL>, AutoCloseable {

  private final String environmentId;
  private SIM_MODEL simulationModel;
  private ENV_MODEL environmentModel;
  private final Map<String, ENT> activeEntities = new LinkedHashMap<>();
  private final Set<Observer<SIM_MODEL, ENT_MODEL>> observers = Sets.newLinkedHashSet();
  private final EntityFactory<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE, ENT> entityFactory;
  private final List<EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL>> behaviors;

  private AbstractEnvironment(String environmentId,
      List<EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL>> behaviors,
      EntityFactory<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE, ENT> entityFactory) {
    this.environmentId = Preconditions.checkNotNull(environmentId);
    this.behaviors = Preconditions.checkNotNull(behaviors);
    this.entityFactory = Preconditions.checkNotNull(entityFactory);
  }

  public void update(SIM_MODEL simulationModel, RandomGenerator random) {
    this.simulationModel = simulationModel;

    for (EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL> behavior : behaviors) {
      behavior.beforeUpdate(simulationModel, environmentModel, this, random);
    }

    // Create a copy of the active entities because entities may be added or removed during the
    // update.
    List<ENT> activeEntityList = new ArrayList<>(activeEntities.values());

    for (ENT entity : activeEntityList) {
      entity.getBehavior().updateInput(environmentModel, random);
    }

    for (EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL> behavior : behaviors) {
      behavior.beforePerformAction(simulationModel, environmentModel, this, random);
    }

    for (ENT entity : activeEntityList) {
      entity.getBehavior().performAction(environmentModel, this, random);
    }

    for (EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL> behavior : behaviors) {
      behavior.afterUpdate(simulationModel, environmentModel, this, random);
    }
  }

  public void setState(SIM_MODEL simulationModel) {
    cleanUp();

    this.simulationModel = simulationModel;

    for (EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL> behavior : behaviors) {
      behavior.setState(simulationModel, environmentModel, this);
    }

    this.environmentModel = getEnvironmentModel(simulationModel, environmentId);

    List<ENT_MODEL> entityModels = getEntities(environmentModel);
    for (ENT_MODEL entityModel : entityModels) {
      addEntity(entityModel);
    }

    for (ENT entity : activeEntities.values()) {
      entity.setState(simulationModel);
    }

    for(EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL> behavior : behaviors) {
      addObserver(behavior);
    }
  }

  @Override
  public ENV_MODEL getModel() {
    return environmentModel;
  }

  public void updateState(SIM_MODEL simulationModel) {
    for (EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL> behavior : behaviors) {
      behavior.updateState(simulationModel, environmentModel, this);
    }
    for (ENT activeEntity : activeEntities.values()) {
      activeEntity.updateState(simulationModel);
    }
  }

  @Override
  public void addEntity(ENT_MODEL entityModel) {
    Optional<ENT> entity = entityFactory.create(entityModel);

    if (entity.isPresent()) {
      if (simulationModel != null) {
        entity.get().setState(simulationModel);
      }
      activeEntities.put(entityModel.getId(), entity.get());
    }
    for (Observer<SIM_MODEL, ENT_MODEL> observer : observers) {
      observer.onAddEntity(entityModel, simulationModel);
    }
  }

  @Override
  public void removeEntity(ENT_MODEL entity) {
    ENT activeEntity = activeEntities.remove(entity.getId());
    if(activeEntity != null) {
      activeEntity.updateState(simulationModel);
    }
    for (Observer<SIM_MODEL, ENT_MODEL> observer : observers) {
      observer.onRemoveEntity(entity, simulationModel);
    }
  }

  public Iterable<ENT_MODEL> getEntities() {
    return Iterables.unmodifiableIterable(getEntities(environmentModel));
  }

  @Override
  public void addObserver(EntityManager.Observer<SIM_MODEL, ENT_MODEL> observer) {
    observers.add(observer);

    for (ENT_MODEL entity : getEntities()) {
      observer.onAddEntity(entity, simulationModel);
    }
  }

  @Override
  public void removeObserver(EntityManager.Observer<SIM_MODEL, ENT_MODEL> observer) {
    observers.remove(observer);
  }

  @Override
  public void close() {
    cleanUp();
  }

  private void cleanUp() {
    observers.removeAll(behaviors);

    // Clean up the active entities.
    for (Entity entity : activeEntities.values()) {
      entity.onRemoveEntity();
    }
    activeEntities.clear();
  }

  protected abstract ENV_MODEL getEnvironmentModel(SIM_MODEL simulationModel, String environmentId);
  protected abstract List<ENT_MODEL> getEntities(ENV_MODEL environmentModel);
}
