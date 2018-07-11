package org.mechaverse.simulation.common;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * An interface for managing the addition and removal of entities from an environment.
 */
@SuppressWarnings("unused")
public interface EntityManager<
    SIM_MODEL extends SimulationModel,
    ENT_MODEL extends EntityModel> {

  interface Observer<SIM_MODEL extends SimulationModel, ENT_MODEL extends EntityModel> {

    void onAddEntity(ENT_MODEL entity, SIM_MODEL state);
    void onRemoveEntity(ENT_MODEL entity, SIM_MODEL state);
  }

  void addEntity(ENT_MODEL entity);
  void removeEntity(ENT_MODEL entity);

  void addObserver(EntityManager.Observer<SIM_MODEL, ENT_MODEL> observer);
  void removeObserver(EntityManager.Observer<SIM_MODEL, ENT_MODEL> observer);

//  ENT_TYPE getType(ENT_MODEL entity);
//
//  ENT_TYPE[] getTypeValues();
}
