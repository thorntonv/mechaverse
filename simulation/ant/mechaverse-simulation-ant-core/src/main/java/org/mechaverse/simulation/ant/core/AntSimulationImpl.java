package org.mechaverse.simulation.ant.core;

import java.io.IOException;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.util.AntSimulationModelUtil;
import org.mechaverse.simulation.common.AbstractSimulation;
import org.mechaverse.simulation.common.EnvironmentFactory;
import org.mechaverse.simulation.common.SimulationModelGenerator;
import org.mechaverse.simulation.common.model.EntityModel;

public class AntSimulationImpl extends AbstractSimulation<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

    public AntSimulationImpl(
            final SimulationModelGenerator<AntSimulationModel> simulationModelGenerator,
            final EnvironmentFactory<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> environmentFactory) {
        super(simulationModelGenerator, environmentFactory);
    }

    @Override
    public void setStateData(byte[] data) throws Exception {
        setState(AntSimulationModelUtil.deserialize(data));
    }

    @Override
    public byte[] getStateData() throws IOException {
        return AntSimulationModelUtil.serialize(getState());
    }
}
