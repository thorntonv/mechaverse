package org.mechaverse.simulation.primordial.core.ui;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.ui.SimulationRenderer;
import org.mechaverse.simulation.common.ui.SimulationVisualizer;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationImpl;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public class PrimordialSimulationVisualizer extends SimulationVisualizer<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

    public PrimordialSimulationVisualizer(final PrimordialSimulationImpl simulation,
            final int cellSize, final int framesPerSecond, final int frameCount, boolean writeImages, String imageBasePath) {
        super(simulation, new SimulationRenderer<>(new PrimordialSimulationImageProvider(), cellSize),
                framesPerSecond, frameCount, writeImages, imageBasePath);
    }
}
