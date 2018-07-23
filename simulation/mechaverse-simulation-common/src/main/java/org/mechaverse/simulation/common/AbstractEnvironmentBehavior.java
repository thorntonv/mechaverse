package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public  class AbstractEnvironmentBehavior<
    SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
    ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>> implements EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> {

  @Override
  public void onAddEntity(ENT_MODEL entity, SIM_MODEL state) {}

  @Override
  public void onRemoveEntity(ENT_MODEL entity, SIM_MODEL state) {}

  @Override
  public void setState(final SIM_MODEL state, final ENV_MODEL environmentModel,
          final EntityManager<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> entityManager) {}

  @Override
  public void updateState(final SIM_MODEL state, final ENV_MODEL environmentModel,
          final EntityManager<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> entityManager) {}

  @Override
  public void beforeUpdate(final SIM_MODEL state, final ENV_MODEL environmentModel,
          final EntityManager<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> entityManager, final RandomGenerator random) {}

  @Override
  public void beforePerformAction(final SIM_MODEL state, final ENV_MODEL environmentModel,
          final EntityManager<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> entityManager, final RandomGenerator random) {}

  @Override
  public void afterUpdate(final SIM_MODEL state, final ENV_MODEL environmentModel,
          final EntityManager<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> entityManager, final RandomGenerator random) {}
}
