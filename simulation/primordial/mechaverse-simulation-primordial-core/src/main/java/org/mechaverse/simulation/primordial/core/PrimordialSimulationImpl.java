package org.mechaverse.simulation.primordial.core;

import org.mechaverse.simulation.common.AbstractSimulation;
import org.mechaverse.simulation.common.EnvironmentFactory;
import org.mechaverse.simulation.common.SimulationModelGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public class PrimordialSimulationImpl extends AbstractSimulation<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

    public PrimordialSimulationImpl() {
        this(new PrimordialSimulationModelGenerator(), new PrimordialEnvironmentFactory());
    }

    public PrimordialSimulationImpl(
            final SimulationModelGenerator<PrimordialSimulationModel> simulationModelGenerator,
            final EnvironmentFactory<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environmentFactory) {
        super(simulationModelGenerator, environmentFactory);
    }
}
