package org.mechaverse.simulation.experimental.ant;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.io.Files;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.SimulationConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.genetic.selection.ElitistSelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.SelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.TournamentSelectionStrategy;
import org.mechaverse.simulation.common.simple.SimpleSimulation;
import org.mechaverse.simulation.common.simple.SimpleSimulationModel;
import org.mechaverse.simulation.common.simple.SimpleSimulationState;

import java.io.File;
import java.io.IOException;

/**
 * A simulation to determine if ants can learn to turn in the direction of food.
 */
public class SimpleAntSimulation {

  private static final int NUM_ENTITIES = 5000;
  private static final int RETAIN_TOP_ENTITY_COUNT = NUM_ENTITIES / 20;
  private static final int REMOVE_BOTTOM_ENTITY_COUNT = NUM_ENTITIES / 20;
  private static final int MAX_ITERATIONS = 10000;
  private static final int UPDATES_PER_ITERATION = 500;


  public static class AntFitnessCalculator implements Function<AntEntity, Double> {

    public static final AntFitnessCalculator INSTANCE = new AntFitnessCalculator();

    @Override
    public Double apply(AntEntity entity) {
      return entity.getFitness();
    }
  }

  private static class AntSupplier implements Supplier<AntEntity> {

    @Override
    public AntEntity get() {
      return new AntEntity();
    }
  }

  private static class Simulation extends SimpleSimulation<AntEntity, SimpleSimulationModel> {

    public Simulation(SimulationConfig<AntEntity, SimpleSimulationModel> config) {
      super(new SimpleSimulationState<>(new SimpleSimulationModel(),
          SimpleSimulationModel.SERIALIZER), config);
    }
  }

  public static void main(String[] args) throws Exception {
    SimulationConfig.Builder<AntEntity, SimpleSimulationModel> configBuilder =
        new SimulationConfig.Builder<>();
    SelectionStrategy<AntEntity> selectionStrategy = new ElitistSelectionStrategy<>(
        RETAIN_TOP_ENTITY_COUNT, REMOVE_BOTTOM_ENTITY_COUNT,
            new TournamentSelectionStrategy<AntEntity>());
    //selectionStrategy = new NoSelectionStrategy<>();

    Simulation simulation = new Simulation(configBuilder
        .setEntitySupplier(new AntSupplier())
        .setEntityFitnessFunction(AntFitnessCalculator.INSTANCE)
        .setUpdatesPerIteration(UPDATES_PER_ITERATION)
        .setOpenCLSimulator(new CellularAutomatonSimulatorConfig.Builder()
            .setNumAutomata(NUM_ENTITIES)
            .setDescriptor(getDescriptor())
            .setAutomatonInputSize(AntEntity.Input.DATA_SIZE)
            .setAutomatonOutputSize(AntEntity.Output.DATA_SIZE)
            .build())
        .setSelectionStrategy(selectionStrategy)
        .build());

    simulation.step(MAX_ITERATIONS, .98);

    AntEntity bestEntity = simulation.getLogger().getOverallBestEntity().getKey();
    final byte[] data = bestEntity.getCellularAutomatonGeneticData().getData();
    Files.write(data, new File("simple-ant.dat"));
  }

  public static CellularAutomatonDescriptor getDescriptor() throws IOException {
    return CellularAutomatonDescriptorReader.read(
        ClassLoader.getSystemResourceAsStream("boolean4.xml"));
  }
}
