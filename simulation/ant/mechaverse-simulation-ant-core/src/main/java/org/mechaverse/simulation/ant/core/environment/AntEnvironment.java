package org.mechaverse.simulation.ant.core.environment;

import java.util.ArrayList;

import org.mechaverse.simulation.ant.core.entity.ant.AntEntityFactory;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.AbstractEnvironment;
import org.mechaverse.simulation.common.model.EntityModel;

public class AntEnvironment extends AbstractEnvironment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

    public AntEnvironment(CellEnvironment env) {
        super(env.getId(), new ArrayList<>(), new AntEntityFactory());
    }
}
