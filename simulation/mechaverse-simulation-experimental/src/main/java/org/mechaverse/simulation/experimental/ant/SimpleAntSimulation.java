package org.mechaverse.simulation.experimental.ant;

import java.util.function.Function;
import java.util.function.Supplier;
import com.google.common.io.Files;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.experimental.simple.SimpleSimulationConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.genetic.selection.ElitistSelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.SelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.TournamentSelectionStrategy;
import org.mechaverse.simulation.experimental.simple.SimpleSimulation;
import org.mechaverse.simulation.experimental.simple.SimpleSimulationModel;
import org.mechaverse.simulation.experimental.simple.SimpleSimulationState;

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


  public static class AntFitnessCalculator implements Function<SimpleAntEntity, Double> {

    public static final AntFitnessCalculator INSTANCE = new AntFitnessCalculator();

    @Override
    public Double apply(SimpleAntEntity entity) {
      return entity.getFitness();
    }
  }

  private static class AntSupplier implements Supplier<SimpleAntEntity> {

    @Override
    public SimpleAntEntity get() {
      return new SimpleAntEntity();
    }
  }

  private static class Simulation extends SimpleSimulation<SimpleAntEntity, SimpleSimulationModel> {

    public Simulation(SimpleSimulationConfig<SimpleAntEntity, SimpleSimulationModel> config) {
      super(new SimpleSimulationState<>(new SimpleSimulationModel(),
          SimpleSimulationModel.SERIALIZER), config);
    }
  }

  public static void main(String[] args) throws Exception {
    SimpleSimulationConfig.Builder<SimpleAntEntity, SimpleSimulationModel> configBuilder =
        new SimpleSimulationConfig.Builder<>();
    SelectionStrategy<SimpleAntEntity> selectionStrategy = new ElitistSelectionStrategy<>(
        RETAIN_TOP_ENTITY_COUNT, REMOVE_BOTTOM_ENTITY_COUNT,
            new TournamentSelectionStrategy<>());
    //selectionStrategy = new NoSelectionStrategy<>();

    Simulation simulation = new Simulation(configBuilder
        .setEntitySupplier(new AntSupplier())
        .setEntityFitnessFunction(AntFitnessCalculator.INSTANCE)
        .setUpdatesPerIteration(UPDATES_PER_ITERATION)
        .setOpenCLSimulator(new CellularAutomatonSimulatorConfig.Builder()
            .setNumAutomata(NUM_ENTITIES)
            .setDescriptor(getDescriptor())
            .setAutomatonInputSize(SimpleAntEntity.Input.DATA_SIZE)
            .setAutomatonOutputSize(SimpleAntEntity.Output.DATA_SIZE)
            .build())
        .setSelectionStrategy(selectionStrategy)
        .build());

    simulation.step(MAX_ITERATIONS, .98);

    SimpleAntEntity bestEntity = simulation.getLogger().getOverallBestEntity().getKey();
    final byte[] data = bestEntity.getCellularAutomatonGeneticData().getData();
    Files.write(data, new File("simple-ant.dat"));
  }

  public static CellularAutomatonDescriptor getDescriptor() throws IOException {
    return CellularAutomatonDescriptorReader.read(
        ClassLoader.getSystemResourceAsStream("boolean4.xml"));
  }
}
