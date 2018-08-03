package org.mechaverse.simulation.primordial.core;

import java.io.IOException;

import org.mechaverse.simulation.common.AbstractSimulation;
import org.mechaverse.simulation.common.EnvironmentFactory;
import org.mechaverse.simulation.common.SimulationModelGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.mechaverse.simulation.primordial.core.util.PrimordialSimulationModelUtil;

public class PrimordialSimulationImpl extends AbstractSimulation<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

    public PrimordialSimulationImpl(
            final SimulationModelGenerator<PrimordialSimulationModel> simulationModelGenerator,
            final EnvironmentFactory<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environmentFactory) {
        super(simulationModelGenerator, environmentFactory);
    }

    @Override
    public void setStateData(byte[] data) throws Exception {
        setState(PrimordialSimulationModelUtil.deserialize(data));
    }

    @Override
    public byte[] getStateData() throws IOException {
        return PrimordialSimulationModelUtil.serialize(getState());
    }
}
