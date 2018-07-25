package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

@SuppressWarnings("unused")
public interface Environment<
        SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
        ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
        ENT_MODEL extends EntityModel<ENT_TYPE>,
        ENT_TYPE extends Enum<ENT_TYPE>> {

  ENV_MODEL getModel();

  void setState(SIM_MODEL simulationModel);

  void update(SIM_MODEL simulationModel, RandomGenerator random);

  void updateState(SIM_MODEL simulationModel);

  void addEntity(ENT_MODEL entity);

  void removeEntity(ENT_MODEL entity);

  void addObserver(SimulationObserver<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> observer);

  void removeObserver(SimulationObserver<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> observer);

  void close();
}
