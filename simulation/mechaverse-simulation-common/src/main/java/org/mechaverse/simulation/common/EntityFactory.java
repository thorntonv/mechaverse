package org.mechaverse.simulation.common;

import java.util.Optional;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * Interface for entity creation.
 */
public interface EntityFactory<
    SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
    ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>> {

    /**
     * Creates an {@link Entity} instance for the given entity model. Returns {@link Optional#EMPTY}
     * if the entity is not an active entity (ie. doesn't have behavior of its own).
     */
    Optional<? extends Entity<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> create(ENT_MODEL entityModel);
}
