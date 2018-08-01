package org.mechaverse.simulation.ant.core.ui;

import java.io.IOException;

import org.mechaverse.simulation.ant.core.AntSimulationImpl;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.ui.SimulationRenderer;
import org.mechaverse.simulation.common.ui.SimulationVisualizer;

public class AntSimulationVisualizer extends SimulationVisualizer<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

    public AntSimulationVisualizer(
            final AntSimulationImpl simulation, final int cellSize, final int framesPerSecond, final int frameCount) throws IOException {
        super(simulation, new SimulationRenderer<>(new AntSimulationImageProvider(), cellSize),
                framesPerSecond, frameCount);
    }
}
