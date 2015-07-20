package org.mechaverse.simulation.bool.majority;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.simple.SimpleSimulation;
import org.mechaverse.simulation.common.simple.SimpleSimulationConfig;
import org.mechaverse.simulation.common.simple.SimpleSimulationModel;
import org.mechaverse.simulation.common.simple.SimpleSimulationState;

import com.google.common.base.Supplier;

/**
 * A {@link SimpleSimulation} implementation of the bitwise majority function. Every iteration each
 * entity is provided with a random N bit number as input. Entities that output the bit value that
 * occurred more frequently on the input are assigned a high fitness.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class MajorityFunctionSimulation
    extends SimpleSimulation<MajorityFunctionEntity, SimpleSimulationModel> {

  private static final int NUM_ENTITIES = 1000;
  private static final int NUM_ITERATIONS = 100000;

  private static class MajorityFunctionEntitySupplier implements Supplier<MajorityFunctionEntity> {

    @Override
    public MajorityFunctionEntity get() {
      return new MajorityFunctionEntity();
    }
  };

  public MajorityFunctionSimulation(
      SimpleSimulationConfig<MajorityFunctionEntity, SimpleSimulationModel> config) {
    super(new SimpleSimulationState<>(
        new SimpleSimulationModel(), SimpleSimulationModel.SERIALIZER), config);
  }

  public static void main(String[] args) throws Exception {
    SimpleSimulationConfig.Builder<MajorityFunctionEntity, SimpleSimulationModel> configBuilder =
        new SimpleSimulationConfig.Builder<MajorityFunctionEntity, SimpleSimulationModel>();

    MajorityFunctionSimulation simulation = new MajorityFunctionSimulation(configBuilder
        .setEntitySupplier(new MajorityFunctionEntitySupplier())
        .setEntityFitnessFunction(MajorityFunctionFitnessCalculator.INSTANCE)
        .setOpenCLSimulator(new CellularAutomatonSimulatorConfig.Builder()
            .setNumAutomata(NUM_ENTITIES)
            .setAutomatonInputSize(MajorityFunctionEntity.ENTITY_INPUT_SIZE)
            .setAutomatonOutputSize(MajorityFunctionEntity.ENTITY_OUTPUT_SIZE)
            .setDescriptorResource("boolean4-small.xml")
            .build())
        .build());

    simulation.step(NUM_ITERATIONS);
  }
}
