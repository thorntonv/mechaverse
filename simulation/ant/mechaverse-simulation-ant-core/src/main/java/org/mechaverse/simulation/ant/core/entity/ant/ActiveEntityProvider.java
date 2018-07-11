package org.mechaverse.simulation.ant.core.entity.ant;

import org.mechaverse.simulation.ant.core.Cell;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.entity.ActiveEntity;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.AbstractActiveEntityProvider;
import org.mechaverse.simulation.common.model.SimulationModel;

public interface ActiveEntityProvider extends AbstractActiveEntityProvider<SimulationModel,
    AntSimulationState, EntityType, Cell, CellEnvironment, ActiveEntity> {
}
