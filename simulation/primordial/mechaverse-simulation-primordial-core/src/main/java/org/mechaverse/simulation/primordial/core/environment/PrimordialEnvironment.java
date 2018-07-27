package org.mechaverse.simulation.primordial.core.environment;

import java.util.ArrayList;

import org.mechaverse.simulation.common.AbstractEnvironment;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.entity.PrimordialEntityFactory;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public class PrimordialEnvironment extends AbstractEnvironment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

    public PrimordialEnvironment(PrimordialEnvironmentModel env) {
        super(env.getId(), new ArrayList<>(), new PrimordialEntityFactory());
    }
}
