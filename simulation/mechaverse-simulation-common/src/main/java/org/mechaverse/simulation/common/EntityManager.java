package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * An interface for managing the addition and removal of entities from an environment.
 */
@SuppressWarnings("unused")
public interface EntityManager<
        SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
        ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
        ENT_MODEL extends EntityModel<ENT_TYPE>,
        ENT_TYPE extends Enum<ENT_TYPE>> {

  interface Observer<SIM_MODEL extends SimulationModel, ENT_MODEL extends EntityModel> {

    void onAddEntity(ENT_MODEL entity, SIM_MODEL state);
    void onRemoveEntity(ENT_MODEL entity, SIM_MODEL state);
  }

  void addEntity(ENT_MODEL entity);
  void removeEntity(ENT_MODEL entity);

  void addObserver(EntityManager.Observer<SIM_MODEL, ENT_MODEL> observer);
  void removeObserver(EntityManager.Observer<SIM_MODEL, ENT_MODEL> observer);
}
