package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.EntityManager.Observer;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public interface Environment<
    SIM_MODEL extends SimulationModel,
    ENV_MODEL extends EnvironmentModel,
    ENT_MODEL extends EntityModel> {

  ENV_MODEL getModel();

  void updateState(SIM_MODEL model);

  void setState(SIM_MODEL model);

  void update(SIM_MODEL model, RandomGenerator random);

  void addObserver(Observer<SIM_MODEL, ENT_MODEL> observer);

  void close();
}
