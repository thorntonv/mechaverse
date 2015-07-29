package org.mechaverse.simulation.bool.majority;

import org.mechaverse.simulation.common.SimulationConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.genetic.selection.ElitistSelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.SelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.TournamentSelectionStrategy;
import org.mechaverse.simulation.common.simple.SimpleSimulation;
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
  private static final int RETAIN_TOP_ENTITY_COUNT = 20;
  private static final int REMOVE_BOTTOM_ENTITY_COUNT = 0;
  private static final int NUM_ITERATIONS = 500;

  private static class MajorityFunctionEntitySupplier implements Supplier<MajorityFunctionEntity> {

    @Override
    public MajorityFunctionEntity get() {
      return new MajorityFunctionEntity();
    }
  };

  public MajorityFunctionSimulation(
      SimulationConfig<MajorityFunctionEntity, SimpleSimulationModel> config) {
    super(new SimpleSimulationState<>(
        new SimpleSimulationModel(), SimpleSimulationModel.SERIALIZER), config);
  }

  public static void main(String[] args) throws Exception {
    SimulationConfig.Builder<MajorityFunctionEntity, SimpleSimulationModel> configBuilder =
        new SimulationConfig.Builder<MajorityFunctionEntity, SimpleSimulationModel>();

    SelectionStrategy<MajorityFunctionEntity> selectionStrategy = new ElitistSelectionStrategy<>(
        RETAIN_TOP_ENTITY_COUNT, REMOVE_BOTTOM_ENTITY_COUNT,
            new TournamentSelectionStrategy<MajorityFunctionEntity>());

    MajorityFunctionSimulation simulation = new MajorityFunctionSimulation(configBuilder
        .setEntitySupplier(new MajorityFunctionEntitySupplier())
        .setEntityFitnessFunction(MajorityFunctionFitnessCalculator.INSTANCE)
        .setOpenCLSimulator(new CellularAutomatonSimulatorConfig.Builder()
            .setNumAutomata(NUM_ENTITIES)
            .setAutomatonInputSize(MajorityFunctionEntity.ENTITY_INPUT_SIZE)
            .setAutomatonOutputSize(MajorityFunctionEntity.ENTITY_OUTPUT_SIZE)
            .setDescriptorResource("boolean4-small.xml")
            .build())
        .setSelectionStrategy(selectionStrategy)
        .build());

    simulation.step(NUM_ITERATIONS);
  }
}
