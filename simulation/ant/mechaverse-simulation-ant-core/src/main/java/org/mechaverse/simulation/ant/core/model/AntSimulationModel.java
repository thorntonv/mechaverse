package org.mechaverse.simulation.ant.core.model;

import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public class AntSimulationModel extends
    SimulationModel<CellEnvironment, EntityModel<EntityType>, EntityType> {

  private int antMaxCount;
  private int antInitialEnergy;
  private int antMinReproductiveAge;
  private int foodMinCount;
  private int foodClusterRadius;
  private int foodInitialEnergy;
  private int pheromoneInitialEnergy;
  private int pheromoneDecayInterval;

  public int getAntMaxCount() {
    return antMaxCount;
  }

  public void setAntMaxCount(int antMaxCount) {
    this.antMaxCount = antMaxCount;
  }

  public int getAntInitialEnergy() {
    return antInitialEnergy;
  }

  public void setAntInitialEnergy(int antInitialEnergy) {
    this.antInitialEnergy = antInitialEnergy;
  }

  public int getAntMinReproductiveAge() {
    return antMinReproductiveAge;
  }

  public void setAntMinReproductiveAge(int antMinReproductiveAge) {
    this.antMinReproductiveAge = antMinReproductiveAge;
  }

  public int getFoodMinCount() {
    return foodMinCount;
  }

  public void setFoodMinCount(int foodMinCount) {
    this.foodMinCount = foodMinCount;
  }

  public int getFoodClusterRadius() {
    return foodClusterRadius;
  }

  public void setFoodClusterRadius(int foodClusterRadius) {
    this.foodClusterRadius = foodClusterRadius;
  }

  public int getFoodInitialEnergy() {
    return foodInitialEnergy;
  }

  public void setFoodInitialEnergy(int foodInitialEnergy) {
    this.foodInitialEnergy = foodInitialEnergy;
  }

  public int getPheromoneInitialEnergy() {
    return pheromoneInitialEnergy;
  }

  public void setPheromoneInitialEnergy(int pheromoneInitialEnergy) {
    this.pheromoneInitialEnergy = pheromoneInitialEnergy;
  }

  public int getPheromoneDecayInterval() {
    return pheromoneDecayInterval;
  }

  public void setPheromoneDecayInterval(int pheromoneDecayInterval) {
    this.pheromoneDecayInterval = pheromoneDecayInterval;
  }
}
