package org.mechaverse.simulation.ant.core;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.environment.AntSimulationEnvironmentGenerator;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.AbstractSimulationModelGenerator;
import org.mechaverse.simulation.common.model.EntityModel;

public class AntSimulationModelGenerator extends
    AbstractSimulationModelGenerator<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> {

  private final int antMaxCount;
  private final int antInitialEnergy;
  private final int antMinReproductiveAge;
  private final int foodMinCount;
  private final int foodClusterRadius;
  private final int foodInitialEnergy;
  private final int pheromoneInitialEnergy;
  private final int pheromoneDecayInterval;

  public AntSimulationModelGenerator(int antMaxCount, int antInitialEnergy,
      int antMinReproductiveAge, int foodMinCount, int foodClusterRadius, int foodInitialEnergy,
      int pheromoneInitialEnergy, int pheromoneDecayInterval, int subEnvironmentCount) {
    super(new AntSimulationEnvironmentGenerator(), subEnvironmentCount);
    this.antMaxCount = antMaxCount;
    this.antInitialEnergy = antInitialEnergy;
    this.antMinReproductiveAge = antMinReproductiveAge;
    this.foodMinCount = foodMinCount;
    this.foodClusterRadius = foodClusterRadius;
    this.foodInitialEnergy = foodInitialEnergy;
    this.pheromoneInitialEnergy = pheromoneInitialEnergy;
    this.pheromoneDecayInterval = pheromoneDecayInterval;
  }

  @Override
  protected AntSimulationModel createModel() {
    return new AntSimulationModel();
  }

  @Override
  public AntSimulationModel generate(RandomGenerator random) {
    AntSimulationModel model = super.generate(random);
    model.setAntMaxCount(antMaxCount);
    model.setAntInitialEnergy(antInitialEnergy);
    model.setAntMinReproductiveAge(antMinReproductiveAge);
    model.setFoodMinCount(foodMinCount);
    model.setFoodClusterRadius(foodClusterRadius);
    model.setFoodInitialEnergy(foodInitialEnergy);
    model.setPheromoneInitialEnergy(pheromoneInitialEnergy);
    model.setPheromoneDecayInterval(pheromoneDecayInterval);
    return model;
  }
}
