package org.mechaverse.simulation.ant.core.entity;

import org.mechaverse.simulation.ant.core.AntSimulationState;
import org.mechaverse.simulation.ant.core.Cell;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveEntityProvider;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.AbstractActiveEntityProviders;
import org.mechaverse.simulation.common.model.SimulationModel;

import java.util.Map;

public class ActiveEntityProviders extends AbstractActiveEntityProviders<SimulationModel,
    AntSimulationState, EntityType, Cell, CellEnvironment, ActiveEntity, ActiveEntityProvider> {

  public ActiveEntityProviders(Map<EntityType, ActiveEntityProvider> activeEntityProviderMap) {
    super(activeEntityProviderMap, new AntEntityFactory());
  }

  @Override
  protected ActiveEntityProvider[] createActiveEntityProviderArray(int length) {
    return new ActiveEntityProvider[length];
  }
}
