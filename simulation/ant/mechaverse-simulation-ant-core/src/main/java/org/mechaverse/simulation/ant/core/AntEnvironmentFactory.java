package org.mechaverse.simulation.ant.core;

import com.google.common.base.Preconditions;
import java.util.List;
import org.mechaverse.simulation.ant.core.environment.AbstractAntEnvironmentBehavior;
import org.mechaverse.simulation.ant.core.environment.AntEnvironment;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.EnvironmentFactory;
import org.mechaverse.simulation.common.model.EntityModel;

public class AntEnvironmentFactory implements EnvironmentFactory<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

    private final List<? extends AbstractAntEnvironmentBehavior> environmentBehaviors;
    private final EntityFactory<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> entityFactory;

    public AntEnvironmentFactory(
        List<? extends AbstractAntEnvironmentBehavior> environmentBehaviors,
        EntityFactory<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> entityFactory) {
        this.environmentBehaviors = Preconditions.checkNotNull(environmentBehaviors);
        this.entityFactory = Preconditions.checkNotNull(entityFactory);
    }

    @Override
    public Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> create(
            final CellEnvironment environmentModel) {
        return new AntEnvironment(environmentModel, environmentBehaviors, entityFactory);
    }
}
