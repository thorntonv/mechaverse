package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public interface EnvironmentFactory<
    SIM_MODEL extends SimulationModel,
    ENV_MODEL extends EnvironmentModel,
    ENT_MODEL extends EntityModel,
    ENV extends Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL>> {

  ENV create(ENV_MODEL environmentModel);
}
