package org.mechaverse.simulation.primordial.core.entity;

import org.mechaverse.simulation.primordial.core.PrimordialSimulationState;
import org.mechaverse.simulation.primordial.core.Cell;
import org.mechaverse.simulation.primordial.core.CellEnvironment;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.common.AbstractActiveEntityProviders;
import org.mechaverse.simulation.common.model.SimulationModel;

import java.util.Map;

public class ActiveEntityProviders extends AbstractActiveEntityProviders<SimulationModel,
    PrimordialSimulationState, EntityType, Cell, CellEnvironment, ActiveEntity, ActiveEntityProvider> {

  public ActiveEntityProviders(Map<EntityType, ActiveEntityProvider> activeEntityProviderMap) {
    super(activeEntityProviderMap, new PrimordialEntityFactory());
  }

  @Override
  protected ActiveEntityProvider[] createActiveEntityProviderArray(int length) {
    return new ActiveEntityProvider[length];
  }
}
