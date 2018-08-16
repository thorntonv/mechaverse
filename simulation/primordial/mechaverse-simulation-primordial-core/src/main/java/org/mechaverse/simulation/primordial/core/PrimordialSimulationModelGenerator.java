package org.mechaverse.simulation.primordial.core;

import org.mechaverse.simulation.common.AbstractSimulationModelGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.environment.PrimordialSimulationEnvironmentGenerator;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public class PrimordialSimulationModelGenerator extends
    AbstractSimulationModelGenerator<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

  public static final int DEFAULT_SUB_ENVIRONMENT_COUNT = 7;

  public PrimordialSimulationModelGenerator() {
    this(DEFAULT_SUB_ENVIRONMENT_COUNT);
  }

  public PrimordialSimulationModelGenerator(int subEnvironmentCount) {
    super(new PrimordialSimulationEnvironmentGenerator(), subEnvironmentCount);
  }

  @Override
  protected PrimordialSimulationModel createModel() {
    PrimordialSimulationModel model = new PrimordialSimulationModel();
    model.setEntityMaxCountPerEnvironment(8192);
    model.setEntityMinReproductiveAge(200);
    model.setEntityInitialEnergy(1200);
    model.setFoodClusterRadius(15);
    model.setFoodMinCount(600);
    model.setFoodInitialEnergy(100);
    model.setMutationRate(.001f);
    return model;
  }
}
