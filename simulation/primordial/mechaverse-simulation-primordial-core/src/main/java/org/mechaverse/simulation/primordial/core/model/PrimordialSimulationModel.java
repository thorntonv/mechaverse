package org.mechaverse.simulation.primordial.core.model;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public class PrimordialSimulationModel
        extends SimulationModel<PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType>
{
    private int entityMaxCount;
    private int entityInitialEnergy;
    private int entityMinReproductiveAge;
    private int foodMinCount;
    private int foodClusterRadius;
    private int foodInitialEnergy;

    public int getFoodClusterRadius() {
        return foodClusterRadius;
    }

    public void setFoodClusterRadius(final int foodClusterRadius) {
        this.foodClusterRadius = foodClusterRadius;
    }

    public int getFoodMinCount() {
        return foodMinCount;
    }

    public void setFoodMinCount(final int foodMinCount) {
        this.foodMinCount = foodMinCount;
    }

    public void setFoodInitialEnergy(final int foodInitialEnergy) {
        this.foodInitialEnergy = foodInitialEnergy;
    }

    public int getFoodInitialEnergy() {
        return foodInitialEnergy;
    }

    public int getEntityMaxCount() {
        return entityMaxCount;
    }

    public void setEntityMaxCount(final int entityMaxCount) {
        this.entityMaxCount = entityMaxCount;
    }

    public int getEntityInitialEnergy() {
        return entityInitialEnergy;
    }

    public void setEntityInitialEnergy(final int entityInitialEnergy) {
        this.entityInitialEnergy = entityInitialEnergy;
    }

    public int getEntityMinReproductiveAge() {
        return entityMinReproductiveAge;
    }

    public void setEntityMinReproductiveAge(final int entityMinReproductiveAge) {
        this.entityMinReproductiveAge = entityMinReproductiveAge;
    }
}
