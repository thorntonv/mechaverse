package org.mechaverse.simulation.primordial.core.entity;

import java.util.Optional;
import java.util.function.Function;

import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import com.google.common.base.Preconditions;


public class PrimordialEntityFactory implements EntityFactory<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

  private final Function<PrimordialEntityModel, PrimordialEntity> primordialEntityFactory;

  public PrimordialEntityFactory(final Function<PrimordialEntityModel, PrimordialEntity> primordialEntityFactory) {
    this.primordialEntityFactory = Preconditions.checkNotNull(primordialEntityFactory);
  }

  @Override
  public Optional<AbstractPrimordialEntity> create(EntityModel<EntityType> entityModel) {
    if(entityModel.getType() == EntityType.ENTITY) {
      return Optional.of(primordialEntityFactory.apply((PrimordialEntityModel) entityModel));
    }
    return Optional.empty();
  }
}
