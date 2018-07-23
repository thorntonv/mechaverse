package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public interface EntityBehavior<
    SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
    ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>> {

  void updateInput(ENV_MODEL env, RandomGenerator random);

  void performAction(ENV_MODEL env,
          EntityManager<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> entityManager,
          RandomGenerator random);

  void setState(SIM_MODEL state);

  void updateState(SIM_MODEL state);

  void onRemoveEntity();
}
