package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.core.environment.AntEnvironment;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.EnvironmentFactory;
import org.mechaverse.simulation.common.model.EntityModel;

public class AntEnvironmentFactory implements EnvironmentFactory<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

    @Override
    public Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> create(
            final CellEnvironment environmentModel) {
        return new AntEnvironment(environmentModel);
    }
}
