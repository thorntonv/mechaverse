package org.mechaverse.simulation.primordial.core.model;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public class PrimordialSimulationModel
        extends SimulationModel<PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType>
{
    private int entityMaxCountPerEnvironment;
    private int entityInitialEnergy;
    private int entityMinReproductiveAge;
    private int foodMinCount;
    private int foodClusterRadius;
    private int foodInitialEnergy;
    private float mutationRate = .001f;

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

    public int getEntityMaxCountPerEnvironment() {
        return entityMaxCountPerEnvironment;
    }

    public void setEntityMaxCountPerEnvironment(final int entityMaxCountPerEnvironment) {
        this.entityMaxCountPerEnvironment = entityMaxCountPerEnvironment;
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

    public float getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(float mutationRate) {
        this.mutationRate = mutationRate;
    }
}
