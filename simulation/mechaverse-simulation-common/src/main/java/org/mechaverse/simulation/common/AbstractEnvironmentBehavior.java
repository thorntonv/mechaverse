package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public abstract class AbstractEnvironmentBehavior<
    SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
    ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>> implements EnvironmentBehavior<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> {

  @Override
  public void setState(final SIM_MODEL state,
          final Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environment) {}

  @Override
  public void updateState(final SIM_MODEL state,
          final Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environment) {}

  @Override
  public void beforeUpdate(final SIM_MODEL state,
          final Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environment,
          final RandomGenerator random) {}

  @Override
  public void beforePerformAction(final SIM_MODEL state,
          final Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environment,
          final RandomGenerator random) {}

  @Override
  public void afterUpdate(final SIM_MODEL state,
          final Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environment,
          final RandomGenerator random) {}

  @Override
  public void onAddEntity(final ENT_MODEL entity, final SIM_MODEL simulationModel, final ENV_MODEL environmentModel) {}

  @Override
  public void onRemoveEntity(final ENT_MODEL entity, final SIM_MODEL simulationModel,
          final ENV_MODEL environmentModel) {}

  @Override
  public void onClose() throws Exception {}
}
