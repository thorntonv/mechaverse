package org.mechaverse.simulation.common;

import java.util.Optional;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * Interface for entity creation.
 */
public interface EntityFactory<
    SIM_MODEL extends SimulationModel,
    ENV_MODEL extends EnvironmentModel,
    ENT_MODEL extends EntityModel,
    ENT_TYPE extends Enum<ENT_TYPE>,
    ENT extends Entity<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> {

    /**
     * Creates an {@link Entity} instance for the given entity model. Returns {@link Optional#EMPTY}
     * if the entity is not an active entity.
     */
    Optional<ENT> create(ENT_MODEL entityModel);

    EntityModel createModel(ENT_TYPE entityType);
}
