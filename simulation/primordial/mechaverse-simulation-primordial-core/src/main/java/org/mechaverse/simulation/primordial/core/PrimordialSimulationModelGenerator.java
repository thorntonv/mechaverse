package org.mechaverse.simulation.primordial.core;

import org.mechaverse.simulation.common.AbstractSimulationModelGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.environment.PrimordialSimulationEnvironmentGenerator;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public class PrimordialSimulationModelGenerator extends AbstractSimulationModelGenerator<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> {

    public PrimordialSimulationModelGenerator() {
        super(new PrimordialSimulationEnvironmentGenerator());
    }

    @Override
    protected PrimordialSimulationModel createModel() {
        PrimordialSimulationModel model = new PrimordialSimulationModel();
        model.setEntityMaxCount(20);
        model.setEntityMinReproductiveAge(10);
        model.setEntityInitialEnergy(100);
        model.setFoodClusterRadius(10);
        model.setFoodMinCount(10);
        model.setFoodInitialEnergy(50);
        return model;
    }
}
