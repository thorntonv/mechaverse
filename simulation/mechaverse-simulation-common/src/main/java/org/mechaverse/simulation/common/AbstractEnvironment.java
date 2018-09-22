package org.mechaverse.simulation.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates an environment.
 */
@SuppressWarnings("unused")
public abstract class AbstractEnvironment<
        SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
        ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
        ENT_MODEL extends EntityModel<ENT_TYPE>,
        ENT_TYPE extends Enum<ENT_TYPE>>
        implements Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>, AutoCloseable {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String environmentId;
  private SIM_MODEL simulationModel;
  private ENV_MODEL environmentModel;
  private final Map<String, Entity<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> activeEntities = new LinkedHashMap<>();
  private final Set<String> activeEntitiesToRemove = new LinkedHashSet<>();
  private final Set<SimulationObserver<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> observers = Sets.newLinkedHashSet();
  private final EntityFactory<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> entityFactory;
  private final List<? extends EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> behaviors;

  protected AbstractEnvironment(String environmentId,
      List<? extends EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> behaviors,
      EntityFactory<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> entityFactory) {
    this.environmentId = Preconditions.checkNotNull(environmentId);
    this.behaviors = Preconditions.checkNotNull(behaviors);
    this.entityFactory = Preconditions.checkNotNull(entityFactory);
  }

  public void update(SIM_MODEL simulationModel, RandomGenerator random) {
    this.simulationModel = simulationModel;

    behaviors.forEach(behavior -> behavior.beforeUpdate(simulationModel, this, random));

    for (Entity<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> activeEntity : activeEntities.values()) {
      activeEntity.getBehavior().updateInput(environmentModel, random);
    }

    behaviors.forEach(behavior -> behavior.beforePerformAction(simulationModel, this, random));

    for (Entity<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> activeEntity : activeEntities.values()) {
      activeEntity.getBehavior().performAction(this, random);
    }

    behaviors.forEach(behavior -> behavior.afterUpdate(simulationModel,this, random));

    // Remove active entities.
    for (String activeEntityToRemove : activeEntitiesToRemove) {
      activeEntities.remove(activeEntityToRemove);
    }
    activeEntitiesToRemove.clear();
  }

  @Override
  public void setState(SIM_MODEL simulationModel) {
    cleanUp();

    this.simulationModel = simulationModel;

    behaviors.forEach(behavior -> behavior.setState(simulationModel, this));

    this.environmentModel = simulationModel.getEnvironment(environmentId);

    environmentModel.getEntities().forEach(this::addEntity);

    activeEntities.values().forEach(activeEntity -> activeEntity.getBehavior().setState(simulationModel));

    behaviors.forEach(this::addObserver);
  }

  @Override
  public ENV_MODEL getModel() {
    return environmentModel;
  }

  public void updateState(SIM_MODEL simulationModel) {
    behaviors.forEach(behavior -> behavior.updateState(simulationModel, this));
    activeEntities.values().forEach(activeEntity -> activeEntity.getBehavior().updateState(simulationModel));
  }

  @Override
  public void addEntity(ENT_MODEL entityModel) {
    Optional<? extends Entity<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> entity = entityFactory.create(entityModel);

    if (entity.isPresent()) {
      if (simulationModel != null) {
        entity.get().getBehavior().setState(simulationModel);
      }
      activeEntities.put(entityModel.getId(), entity.get());
    }
    observers.forEach(observer -> observer.onAddEntity(entityModel, simulationModel, environmentModel));
  }

  @Override
  public void removeEntity(ENT_MODEL entity) {
    if (entity.getId() != null) {
      activeEntitiesToRemove.add(entity.getId());
    }

    environmentModel.remove(entity);

    observers.forEach(observer -> observer.onRemoveEntity(entity, simulationModel, environmentModel));
  }

  public Iterable<ENT_MODEL> getEntities() {
    return Iterables.unmodifiableIterable(environmentModel.getEntities());
  }

  @Override
  public void addObserver(SimulationObserver<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> observer) {
    observers.add(observer);

    getEntities().forEach(entity -> observer.onAddEntity(entity, simulationModel, environmentModel));
  }

  @Override
  public void removeObserver(SimulationObserver<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> observer) {
    observers.remove(observer);
  }

  @Override
  public void close() {
    cleanUp();
  }

  private void cleanUp() {
    observers.forEach(observer -> {
      try {
        observer.onClose();
      } catch (Exception e) {
        logger.error("Error closing simulation", e);
      }
    });
    observers.removeAll(behaviors);

    // Clean up the active entities.
    activeEntities.clear();
  }
}
