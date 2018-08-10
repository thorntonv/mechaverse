package org.mechaverse.simulation.primordial.core;

import org.mechaverse.simulation.common.EnvironmentFactory;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public abstract class PrimordialEnvironmentFactory implements
    EnvironmentFactory<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {
}
