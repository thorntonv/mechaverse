package org.mechaverse.simulation.ant.core;

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Nest;

import com.google.common.collect.Sets;

/**
 * A nest that generates ants to maintain a target ant count.
 */
public class ActiveNest implements ActiveEntity, EntityManager.Observer {

  // TODO(thorntonv): Make these values configurable.
  private final int targetAntCount = 500;
  private final int initialAntEnergy = 1000;

  private final Nest nest;
  private final Set<Entity> ants = Sets.newIdentityHashSet();
  private Cell cell;

  public ActiveNest(Nest entity) {
    this.nest = entity;
  }

  @Override
  public void updateInput(CellEnvironment env, RandomGenerator random) {}

  @Override
  public void performAction(
      CellEnvironment env, EntityManager entityManager, RandomGenerator random) {
    if(cell == null) {
      cell = env.getCell(nest);
      entityManager.removeObserver(this);
    }

    Entity food = cell.getEntity(EntityType.FOOD);

    if (food != null) {
      int energy = nest.getEnergy() + food.getEnergy();
      nest.setEnergy(energy <= nest.getMaxEnergy() ? energy : nest.getMaxEnergy());
      cell.removeEntity(food);
      entityManager.removeEntity(food);
    }

    if (ants.size() < targetAntCount && !cell.hasEntity(EntityType.ANT)) {
      Ant ant = new Ant();
      ant.setDirection(AntSimulationUtil.randomDirection(random));
      cell.setEntity(ant, EntityType.ANT);
      ant.setMaxEnergy(initialAntEnergy);
      ant.setEnergy(ant.getMaxEnergy());
      entityManager.addEntity(ant);
    }
  }

  @Override
  public Entity getEntity() {
    return nest;
  }

  @Override
  public EntityType getType() {
    return EntityType.NEST;
  }

  @Override
  public void updateModel() {}

  @Override
  public void onAddEntity(Entity entity) {
    if (entity instanceof Ant) {
      ants.add(entity);
    }
  }

  @Override
  public void onRemoveEntity(Entity entity) {
    if (entity instanceof Ant) {
      ants.remove(entity);
    }
  }
}
