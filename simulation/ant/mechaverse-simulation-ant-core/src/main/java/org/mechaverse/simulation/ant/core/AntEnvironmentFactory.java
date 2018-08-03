package org.mechaverse.simulation.ant.core;

import java.util.List;

import org.mechaverse.simulation.ant.core.entity.ant.AntEntityFactory;
import org.mechaverse.simulation.ant.core.environment.AbstractAntEnvironmentBehavior;
import org.mechaverse.simulation.ant.core.environment.AntEnvironment;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.EnvironmentFactory;
import org.mechaverse.simulation.common.model.EntityModel;
import com.google.common.base.Preconditions;

public class AntEnvironmentFactory implements EnvironmentFactory<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

    private final List<? extends AbstractAntEnvironmentBehavior> environmentBehaviors;
    private final AntEntityFactory entityFactory;

    public AntEnvironmentFactory(
        List<? extends AbstractAntEnvironmentBehavior> environmentBehaviors,
            AntEntityFactory entityFactory) {
        this.environmentBehaviors = Preconditions.checkNotNull(environmentBehaviors);
        this.entityFactory = Preconditions.checkNotNull(entityFactory);
    }

    @Override
    public Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> create(
            final CellEnvironment environmentModel) {
        return new AntEnvironment(environmentModel, environmentBehaviors, entityFactory);
    }
}
