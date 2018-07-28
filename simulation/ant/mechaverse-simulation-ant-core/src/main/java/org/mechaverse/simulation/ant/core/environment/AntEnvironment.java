package org.mechaverse.simulation.ant.core.environment;

import java.util.List;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.AbstractEnvironment;
import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.model.EntityModel;

public class AntEnvironment extends
    AbstractEnvironment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

  public AntEnvironment(CellEnvironment env, List<? extends AbstractAntEnvironmentBehavior> environmentBehaviors,
      EntityFactory<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> entityFactory) {
    super(env.getId(), environmentBehaviors, entityFactory);
  }
}
