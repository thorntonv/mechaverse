package org.mechaverse.simulation.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;
import org.mechaverse.simulation.common.genetic.CutAndSpliceCrossoverGeneticRecombinator;
import org.mechaverse.simulation.common.genetic.GeneticRecombinator;
import org.mechaverse.simulation.common.genetic.selection.FitnessProportionalSelectionStrategy;
import org.mechaverse.simulation.common.genetic.selection.SelectionStrategy;
import org.mechaverse.simulation.common.simple.SimpleSimulationLogger;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

public class SimulationConfig<E extends AbstractEntity, M> {

  public static class Builder<E extends AbstractEntity, M> {

    private Supplier<E> entitySupplier;
    private Function<E, Double> entityFitnessFunction;
    private CellularAutomatonSimulationModel cellularAutomatonModel;
    private CellularAutomatonSimulator simulator;
    private SelectionStrategy<E> selectionStrategy = new FitnessProportionalSelectionStrategy<>();
    private GeneticRecombinator geneticRecombinator = new CutAndSpliceCrossoverGeneticRecombinator();
    private int updatesPerIteration = 100;
    private SimulationLogger<E, M> simulationLogger;
    private boolean minimize = false;

    public Builder<E, M> setEntitySupplier(Supplier<E> entitySupplier) {
      this.entitySupplier = entitySupplier;
      return this;
    }

    public Builder<E, M> setEntityFitnessFunction(Function<E, Double> entityFitnessFunction) {
      this.entityFitnessFunction = entityFitnessFunction;
      return this;
    }

    public Builder<E, M> setMinimize(boolean minimize) {
      this.minimize = minimize;
      return this;
    }

    public Builder<E, M> setOpenCLSimulator(CellularAutomatonSimulatorConfig simulatorConfig) {
      this.cellularAutomatonModel =
          CellularAutomatonSimulationModelBuilder.build(simulatorConfig.getDescriptor());
      this.simulator = new OpenClCellularAutomatonSimulator(simulatorConfig);
      return this;
    }

    public Builder<E, M> setSelectionStrategy(SelectionStrategy<E> selectionStrategy) {
      this.selectionStrategy = selectionStrategy;
      return this;
    }

    public Builder<E, M> setGeneticRecombinator(GeneticRecombinator geneticRecombinator) {
      this.geneticRecombinator = geneticRecombinator;
      return this;
    }

    public Builder<E, M> setSimulationLogger(String filename,
          Function<E, Double> entityFitnessFunction) throws FileNotFoundException {
      PrintWriter results = new PrintWriter(new File(filename));
      return setSimulationLogger(new SimpleSimulationLogger<E, M>(results, entityFitnessFunction));
    }

    public Builder<E, M> setSimulationLogger(SimulationLogger<E, M> simulationLogger) {
      this.simulationLogger = simulationLogger;
      return this;
    }

    public Builder<E, M> setUpdatesPerIteration(int updatesPerIteration) {
      this.updatesPerIteration = updatesPerIteration;
      return this;
    }

    public SimulationConfig<E, M> build() {
      if (simulationLogger == null) {
        try {
          setSimulationLogger("results.csv", entityFitnessFunction);
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
      simulationLogger.setMinimize(minimize);
      selectionStrategy.setMinimize(minimize);
      return new SimulationConfig<>(entitySupplier, entityFitnessFunction, cellularAutomatonModel, simulator, selectionStrategy, geneticRecombinator, simulationLogger, updatesPerIteration);
    }
  }

  private final Supplier<E> entitySupplier;
  private final Function<E, Double> entityFitnessFunction;
  private final CellularAutomatonSimulationModel cellularAutomatonModel;
  private final CellularAutomatonSimulator simulator;
  private final SelectionStrategy<E> selectionStrategy;
  private final GeneticRecombinator geneticRecombinator;
  private final SimulationLogger<E, M> simulationLogger;
  private final int updatesPerIteration;

  public SimulationConfig(Supplier<E> entitySupplier,
      Function<E, Double> entityFitnessFunction,
      CellularAutomatonSimulationModel cellularAutomatonModel, CellularAutomatonSimulator simulator,
      SelectionStrategy<E> selectionStrategy, GeneticRecombinator geneticRecombinator,
      SimulationLogger<E, M> simulationLogger, int updatesPerIteration) {
    this.entitySupplier = entitySupplier;
    this.entityFitnessFunction = entityFitnessFunction;
    this.cellularAutomatonModel = cellularAutomatonModel;
    this.simulator = simulator;
    this.selectionStrategy = selectionStrategy;
    this.geneticRecombinator = geneticRecombinator;
    this.simulationLogger = simulationLogger;
    this.updatesPerIteration = updatesPerIteration;
  }

  public Supplier<E> getEntitySupplier() {
    return entitySupplier;
  }

  public Function<E, Double> getEntityFitnessFunction() {
    return entityFitnessFunction;
  }

  public CellularAutomatonSimulationModel getCellularAutomatonModel() {
    return cellularAutomatonModel;
  }

  public CellularAutomatonSimulator getSimulator() {
    return simulator;
  }

  public SelectionStrategy<E> getSelectionStrategy() {
    return selectionStrategy;
  }

  public GeneticRecombinator getGeneticRecombinator() {
    return geneticRecombinator;
  }

  public SimulationLogger<E, M> getSimulationLogger() {
    return simulationLogger;
  }

  public int getUpdatesPerIteration() {
    return updatesPerIteration;
  }
}
