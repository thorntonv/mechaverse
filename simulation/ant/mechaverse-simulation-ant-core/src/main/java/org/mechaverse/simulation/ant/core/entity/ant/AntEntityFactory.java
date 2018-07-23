package org.mechaverse.simulation.ant.core.entity.ant;

import java.util.Optional;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.model.EntityModel;

public class AntEntityFactory implements EntityFactory<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

  @Override
  public Optional<AbstractAntEntity> create(EntityModel<EntityType> entityModel) {
    if(entityModel.getType() == EntityType.ANT) {
      return Optional.of(new AntEntity());
    }
    return Optional.empty();
  }
}
