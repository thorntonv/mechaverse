package org.mechaverse.simulation.primordial.core;

import org.mechaverse.simulation.common.AbstractSimulationModelGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.environment.PrimordialSimulationEnvironmentGenerator;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public class PrimordialSimulationModelGenerator extends AbstractSimulationModelGenerator<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

    protected PrimordialSimulationModelGenerator() {
        super(new PrimordialSimulationEnvironmentGenerator());
    }

    @Override
    protected PrimordialSimulationModel createModel() {
        return new PrimordialSimulationModel();
    }
}
