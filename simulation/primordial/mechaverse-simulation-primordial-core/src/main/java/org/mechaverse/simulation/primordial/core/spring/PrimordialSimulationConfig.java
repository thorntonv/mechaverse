package org.mechaverse.simulation.primordial.core.spring;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Function;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.SimulationModelGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator.CellularAutomatonSimulatorParams;
import org.mechaverse.simulation.common.cellautomaton.simulation.NoOpCellularAutomatonSimulator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.PrimordialEnvironmentFactory;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationImpl;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationModelGenerator;
import org.mechaverse.simulation.primordial.core.entity.PrimordialEntityFactory;
import org.mechaverse.simulation.primordial.core.environment.CellularAutomatonSimulationBehavior;
import org.mechaverse.simulation.primordial.core.environment.EntityReproductionBehavior;
import org.mechaverse.simulation.primordial.core.environment.FoodGenerationBehavior;
import org.mechaverse.simulation.primordial.core.environment.PrimordialEnvironment;
import org.mechaverse.simulation.primordial.core.environment.PrimordialEnvironmentBehavior;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@SuppressWarnings({"unused", "WeakerAccess"})
public class PrimordialSimulationConfig {

  @Bean
  @Scope("prototype")
  public PrimordialSimulationImpl simulation(
      SimulationModelGenerator<PrimordialSimulationModel> simulationModelGenerator,
          PrimordialEnvironmentFactory environmentFactory) {
    return new PrimordialSimulationImpl(simulationModelGenerator, environmentFactory);
  }

  @Bean
  @Scope("prototype")
  public SimulationModelGenerator<PrimordialSimulationModel> simulationModelGenerator() {
    return new PrimordialSimulationModelGenerator();
  }

  @Bean
  @Scope("prototype")
  public FoodGenerationBehavior foodGenerationBehavior() {
    return new FoodGenerationBehavior();
  }

  @Bean
  @Scope("prototype")
  public EntityReproductionBehavior reproductionBehavior() {
    return new EntityReproductionBehavior();
  }

  @Bean
  @Scope("prototype")
  public CellularAutomatonSimulationBehavior cellularAutomatonSimulationBehavior() {
    return new CellularAutomatonSimulationBehavior();
  }

  @Bean
  @Scope("prototype")
  public PrimordialEnvironmentFactory environmentFactory() {
    return new PrimordialEnvironmentFactory() {
      @Override
      public Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> create(
          PrimordialEnvironmentModel environmentModel) {
        CellularAutomatonSimulationBehavior cellularAutomatonBehavior = cellularAutomatonSimulationBehavior();
        List<PrimordialEnvironmentBehavior> environmentBehaviors = ImmutableList.of(
            foodGenerationBehavior(),
            reproductionBehavior(),
            cellularAutomatonBehavior);
        return new PrimordialEnvironment(environmentModel, environmentBehaviors, entityFactory());
      }
    };
  }

//  @Bean
//  @Scope("prototype")
//  public Function<CellularAutomatonSimulatorParams, CellularAutomatonSimulator> cellularAutomatonSimulatorFactory() {
//    return params -> new BitwiseCellularAutomatonSimulator(
//            new OpenClCellularAutomatonSimulator(IntMath.divide(params.numAutomata, Integer.SIZE, RoundingMode.CEILING),
//                    PrimordialEntityInput.DATA_SIZE_BITS, PrimordialEntityOutput.DATA_SIZE_BITS,
//                    CLPlatform.getDefault().getMaxFlopsDevice(), params.descriptorDataSource.getDescriptor()), 1);
//  }

  @Bean
  @Scope("prototype")
  public Function<CellularAutomatonSimulatorParams, CellularAutomatonSimulator> cellularAutomatonSimulatorFactory() {
    return params -> new NoOpCellularAutomatonSimulator(params.numAutomata, 1, 500, 1);
  }

  private PrimordialEntityFactory entityFactory() {
    return new PrimordialEntityFactory();
  }
}
