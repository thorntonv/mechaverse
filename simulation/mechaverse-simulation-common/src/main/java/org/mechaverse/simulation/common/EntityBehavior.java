package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public interface EntityBehavior<
    SIM_MODEL extends SimulationModel,
    ENV_MODEL extends EnvironmentModel,
    ENT_MODEL extends EntityModel> {

  void updateInput(ENV_MODEL env, RandomGenerator random);
  void performAction(ENV_MODEL env, EntityManager<SIM_MODEL, ENT_MODEL> entityManager, RandomGenerator random);
}
