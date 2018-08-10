package org.mechaverse.simulation.primordial.core;

import org.mechaverse.simulation.common.AbstractSimulationModelGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.environment.PrimordialSimulationEnvironmentGenerator;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public class PrimordialSimulationModelGenerator extends
    AbstractSimulationModelGenerator<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

  public static final int DEFAULT_SUB_ENVIRONMENT_COUNT = 3;

  public PrimordialSimulationModelGenerator() {
    super(new PrimordialSimulationEnvironmentGenerator(), DEFAULT_SUB_ENVIRONMENT_COUNT);
  }

  @Override
  protected PrimordialSimulationModel createModel() {
    PrimordialSimulationModel model = new PrimordialSimulationModel();
    model.setEntityMaxCountPerEnvironment(1024);
    model.setEntityMinReproductiveAge(10);
    model.setEntityInitialEnergy(200);
    model.setFoodClusterRadius(15);
    model.setFoodMinCount(600);
    model.setFoodInitialEnergy(100);
    return model;
  }
}
