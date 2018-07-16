package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.AbstractEnvironmentBehavior;
import org.mechaverse.simulation.common.model.EntityModel;

public abstract class AbstractAntEnvironmentBehavior extends
    AbstractEnvironmentBehavior<AntSimulationModel, CellEnvironment, EntityModel<EntityType>> {
}
