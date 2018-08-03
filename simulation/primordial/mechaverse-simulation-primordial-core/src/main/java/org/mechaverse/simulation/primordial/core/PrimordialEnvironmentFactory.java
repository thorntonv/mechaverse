package org.mechaverse.simulation.primordial.core;

import java.util.List;

import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.EnvironmentFactory;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.entity.PrimordialEntityFactory;
import org.mechaverse.simulation.primordial.core.environment.PrimordialEnvironment;
import org.mechaverse.simulation.primordial.core.environment.PrimordialEnvironmentBehavior;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public class PrimordialEnvironmentFactory implements EnvironmentFactory<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

    private final List<? extends PrimordialEnvironmentBehavior> environmentBehaviors;
    private final PrimordialEntityFactory entityFactory;

    public PrimordialEnvironmentFactory(
            final List<? extends PrimordialEnvironmentBehavior> environmentBehaviors,
            final PrimordialEntityFactory entityFactory) {
        this.environmentBehaviors = environmentBehaviors;
        this.entityFactory = entityFactory;
    }

    @Override
    public Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> create(
            final PrimordialEnvironmentModel environmentModel) {
        return new PrimordialEnvironment(environmentModel, environmentBehaviors, entityFactory);
    }
}
