package org.mechaverse.simulation.ant.core.spring;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Function;
import org.mechaverse.simulation.ant.core.AntEnvironmentFactory;
import org.mechaverse.simulation.ant.core.AntSimulationImpl;
import org.mechaverse.simulation.ant.core.AntSimulationModelGenerator;
import org.mechaverse.simulation.ant.core.entity.ant.AntEntity;
import org.mechaverse.simulation.ant.core.entity.ant.AntEntityFactory;
import org.mechaverse.simulation.ant.core.entity.ant.SimpleAntBehavior;
import org.mechaverse.simulation.ant.core.environment.AbstractAntEnvironmentBehavior;
import org.mechaverse.simulation.ant.core.environment.AntEnvironment;
import org.mechaverse.simulation.ant.core.environment.AntReproductionBehavior;
import org.mechaverse.simulation.ant.core.environment.FoodGenerationBehavior;
import org.mechaverse.simulation.ant.core.environment.PheromoneDecayBehavior;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.EnvironmentFactory;
import org.mechaverse.simulation.common.SimulationModelGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@SuppressWarnings("unused")
public class SimpleAntSimulationConfig {

  private static final int ANT_MAX_COUNT = 500;
  private static final int ANT_INITIAL_ENERGY = 100;
  private static final int ANT_MIN_REPRODUCTIVE_AGE = 25;
  private static final int FOOD_MIN_COUNT = 1000;
  private static final int FOOD_CLUSTER_RADIUS = 15;
  private static final int FOOD_INITIAL_ENERGY = 10;
  private static final int PHEROMONE_INITIAL_ENERGY = 100;
  private static final int PHEROMONE_DECAY_INTERVAL = 100;
  private static final int SUB_ENVIRONMENT_COUNT = 0;

  @Bean
  @Scope("prototype")
  public AntSimulationImpl simulation(
      SimulationModelGenerator<AntSimulationModel> simulationModelGenerator,
      EnvironmentFactory<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> environmentFactory) {
    return new AntSimulationImpl(simulationModelGenerator, environmentFactory);
  }

  @Bean
  @Scope("prototype")
  public SimulationModelGenerator<AntSimulationModel> simulationModelGenerator() {
    return new AntSimulationModelGenerator(ANT_MAX_COUNT, ANT_INITIAL_ENERGY,
        ANT_MIN_REPRODUCTIVE_AGE, FOOD_MIN_COUNT, FOOD_CLUSTER_RADIUS, FOOD_INITIAL_ENERGY,
        PHEROMONE_INITIAL_ENERGY, PHEROMONE_DECAY_INTERVAL, SUB_ENVIRONMENT_COUNT);
  }

  @Bean
  @Scope("prototype")
  public AntEnvironmentFactory environmentFactory(AntEntityFactory entityFactory) {
    return new AntEnvironmentFactory(){
      @Override
      public Environment<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> create(
          CellEnvironment environmentModel) {
        List<AbstractAntEnvironmentBehavior> environmentBehaviors = ImmutableList.of(
            new FoodGenerationBehavior(),
            new PheromoneDecayBehavior(),
            new AntReproductionBehavior());
        return new AntEnvironment(environmentModel, environmentBehaviors, entityFactory);
      }
    };
  }

  @Bean
  public AntEntityFactory entityFactory(Function<Ant, AntEntity> antEntityFactory) {
    return new AntEntityFactory(antEntityFactory);
  }

  @Bean
  public Function<Ant, AntEntity> antEntityFactory() {
    return (ant) -> new AntEntity(new SimpleAntBehavior(ant));
  }
}
