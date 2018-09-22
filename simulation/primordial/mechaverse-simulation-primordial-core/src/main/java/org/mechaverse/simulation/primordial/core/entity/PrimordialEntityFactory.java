package org.mechaverse.simulation.primordial.core.entity;

import java.util.Optional;
import org.mechaverse.simulation.common.Entity;
import org.mechaverse.simulation.common.EntityFactory;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public class PrimordialEntityFactory implements EntityFactory<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

  @Override
  public Optional<? extends Entity<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType>> create(EntityModel<EntityType> entityModel) {
    return Optional.empty();
  }
}
