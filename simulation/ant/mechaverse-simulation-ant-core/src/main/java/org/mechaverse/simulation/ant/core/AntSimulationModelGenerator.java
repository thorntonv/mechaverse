package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.core.environment.AntSimulationEnvironmentGenerator;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.AbstractSimulationModelGenerator;
import org.mechaverse.simulation.common.model.EntityModel;

public class AntSimulationModelGenerator extends AbstractSimulationModelGenerator<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

    protected AntSimulationModelGenerator() {
        super(new AntSimulationEnvironmentGenerator());
    }

    @Override
    protected AntSimulationModel createModel() {
        return new AntSimulationModel();
    }
}
