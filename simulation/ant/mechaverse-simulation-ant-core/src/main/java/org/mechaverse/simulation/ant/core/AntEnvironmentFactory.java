package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.EnvironmentFactory;
import org.mechaverse.simulation.common.model.EntityModel;

public abstract class AntEnvironmentFactory implements EnvironmentFactory<
    AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {
}
