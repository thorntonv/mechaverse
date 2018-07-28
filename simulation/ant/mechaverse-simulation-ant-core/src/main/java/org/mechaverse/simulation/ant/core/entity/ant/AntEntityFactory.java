package org.mechaverse.simulation.ant.core.entity.ant;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.function.Function;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.model.EntityModel;

public class AntEntityFactory implements EntityFactory<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

  private final Function<Ant, AntEntity> antEntityFactory;

  public AntEntityFactory(final Function<Ant, AntEntity> antEntityFactory) {
    this.antEntityFactory = Preconditions.checkNotNull(antEntityFactory);
  }

  @Override
  public Optional<AbstractAntEntity> create(EntityModel<EntityType> entityModel) {
    if(entityModel.getType() == EntityType.ANT) {
      return Optional.of(antEntityFactory.apply((Ant) entityModel));
    }
    return Optional.empty();
  }
}
