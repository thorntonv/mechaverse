package org.mechaverse.simulation.primordial.core.spring;

import com.google.common.collect.ImmutableList;
import com.jogamp.opencl.CLPlatform;
import java.util.List;
import java.util.function.Function;
import org.mechaverse.simulation.common.SimulationModelGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator.CellularAutomatonSimulatorParams;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;
import org.mechaverse.simulation.primordial.core.PrimordialEnvironmentFactory;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationImpl;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationModelGenerator;
import org.mechaverse.simulation.primordial.core.entity.CellularAutomatonPrimordialEntityBehavior;
import org.mechaverse.simulation.primordial.core.entity.PrimordialEntity;
import org.mechaverse.simulation.primordial.core.entity.PrimordialEntityFactory;
import org.mechaverse.simulation.primordial.core.entity.PrimordialEntityInput;
import org.mechaverse.simulation.primordial.core.environment.CellularAutomatonSimulationBehavior;
import org.mechaverse.simulation.primordial.core.environment.EntityReproductionBehavior;
import org.mechaverse.simulation.primordial.core.environment.FoodGenerationBehavior;
import org.mechaverse.simulation.primordial.core.environment.PrimordialEnvironmentBehavior;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;
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
    CellularAutomatonSimulationBehavior cellularAutomatonBehavior = cellularAutomatonSimulationBehavior();
    List<PrimordialEnvironmentBehavior> environmentBehaviors = ImmutableList.of(
        foodGenerationBehavior(),
            reproductionBehavior(),
        cellularAutomatonBehavior);
    return new PrimordialEnvironmentFactory(environmentBehaviors, entityFactory(cellularAutomatonBehavior));
  }

  @Bean
  @Scope("prototype")
  public Function<CellularAutomatonSimulatorParams, CellularAutomatonSimulator> cellularAutomatonSimulatorFactory() {
    return params -> new OpenClCellularAutomatonSimulator(params.numAutomata, PrimordialEntityInput.DATA_SIZE, 32,
        CLPlatform.getDefault().getMaxFlopsDevice(), params.descriptorDataSource.getDescriptor());
  }

  private PrimordialEntityFactory entityFactory(
      CellularAutomatonSimulationBehavior cellularAutomatonBehavior) {
    return new PrimordialEntityFactory(antEntityFactory(cellularAutomatonBehavior));
  }

  private Function<PrimordialEntityModel, PrimordialEntity> antEntityFactory(
      CellularAutomatonSimulationBehavior cellularAutomatonBehavior) {
    return (entity) -> new PrimordialEntity(entity, new CellularAutomatonPrimordialEntityBehavior(entity,
        cellularAutomatonBehavior.getDescriptorDataSource(),
        cellularAutomatonBehavior.getSimulator()));
  }
}
