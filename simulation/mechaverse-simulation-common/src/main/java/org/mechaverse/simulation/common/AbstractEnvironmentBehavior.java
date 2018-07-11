package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public abstract class AbstractEnvironmentBehavior<
    SIM_MODEL extends SimulationModel,
    ENV_MODEL extends EnvironmentModel,
    ENT_MODEL extends EntityModel> implements EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL> {

  @Override
  public void setState(SIM_MODEL state, ENV_MODEL environmentModel,
      EntityManager<SIM_MODEL, ENT_MODEL> entityManager) {}

  @Override
  public void updateState(SIM_MODEL state, ENV_MODEL environmentModel,
      EntityManager<SIM_MODEL, ENT_MODEL> entityManager) {}

  @Override
  public void beforeUpdate(SIM_MODEL state, ENV_MODEL environmentModel,
      EntityManager<SIM_MODEL, ENT_MODEL> entityManager, RandomGenerator random) {}

  @Override
  public void beforePerformAction(SIM_MODEL state, ENV_MODEL environmentModel,
      EntityManager<SIM_MODEL, ENT_MODEL> entityManager, RandomGenerator random) {}

  @Override
  public void afterUpdate(SIM_MODEL state, ENV_MODEL environmentModel,
      EntityManager<SIM_MODEL, ENT_MODEL> entityManager, RandomGenerator random) {}

  @Override
  public void onAddEntity(ENT_MODEL entity, SIM_MODEL state) {}

  @Override
  public void onRemoveEntity(ENT_MODEL entity, SIM_MODEL state) {}
}
