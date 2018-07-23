package org.mechaverse.simulation.common;

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
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Simulates an environment.
 */
@SuppressWarnings("unused")
public abstract class AbstractEnvironment<
        SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
        ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
        ENT_MODEL extends EntityModel<ENT_TYPE>,
        ENT_TYPE extends Enum<ENT_TYPE>>
        implements Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>,
        EntityManager<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>, AutoCloseable {

  private final String environmentId;
  private SIM_MODEL simulationModel;
  private ENV_MODEL environmentModel;
  private final Map<String, Entity<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> activeEntities = new LinkedHashMap<>();
  private final Set<Observer<SIM_MODEL, ENT_MODEL>> observers = Sets.newLinkedHashSet();
  private final EntityFactory<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> entityFactory;
  private final List<EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> behaviors;

  protected AbstractEnvironment(String environmentId,
      List<EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> behaviors,
      EntityFactory<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> entityFactory) {
    this.environmentId = Preconditions.checkNotNull(environmentId);
    this.behaviors = Preconditions.checkNotNull(behaviors);
    this.entityFactory = Preconditions.checkNotNull(entityFactory);
  }

  public void update(SIM_MODEL simulationModel, RandomGenerator random) {
    this.simulationModel = simulationModel;

    behaviors.forEach(behavior -> behavior.beforeUpdate(simulationModel, environmentModel, this, random));

    // Create a copy of the active entities because entities may be added or removed during the
    // update.
    List<Entity<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> activeEntityList = new ArrayList<>(activeEntities.values());

    activeEntityList.forEach(activeEntity -> activeEntity.getBehavior().updateInput(environmentModel, random));

    behaviors.forEach(behavior -> behavior.beforePerformAction(simulationModel, environmentModel, this, random));

    activeEntityList.forEach(activeEntity -> activeEntity.getBehavior().performAction(environmentModel, this, random));

    behaviors.forEach(behavior -> behavior.afterUpdate(simulationModel, environmentModel, this, random));
  }

  @Override
  public void setState(SIM_MODEL simulationModel) {
    cleanUp();

    this.simulationModel = simulationModel;

    behaviors.forEach(behavior -> behavior.setState(simulationModel, environmentModel, this));

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
    behaviors.forEach(behavior -> behavior.updateState(simulationModel, environmentModel, this));
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
    observers.forEach(observer -> observer.onAddEntity(entityModel, simulationModel));
  }

  @Override
  public void removeEntity(ENT_MODEL entity) {
    Entity<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> activeEntity = activeEntities.remove(entity.getId());
    if(activeEntity != null) {
      activeEntity.getBehavior().updateState(simulationModel);
    }
    observers.forEach(observer -> observer.onRemoveEntity(entity, simulationModel));
  }

  public Iterable<ENT_MODEL> getEntities() {
    return Iterables.unmodifiableIterable(environmentModel.getEntities());
  }

  @Override
  public void addObserver(EntityManager.Observer<SIM_MODEL, ENT_MODEL> observer) {
    observers.add(observer);

    getEntities().forEach(entity -> observer.onAddEntity(entity, simulationModel));
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
    activeEntities.values().forEach(activeEntity -> activeEntity.getBehavior().onRemoveEntity());
    activeEntities.clear();
  }
}
