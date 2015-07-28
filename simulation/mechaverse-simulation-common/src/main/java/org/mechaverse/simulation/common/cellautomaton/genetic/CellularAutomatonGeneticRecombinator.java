package org.mechaverse.simulation.common.cellautomaton.genetic;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticRecombinator;

/**
 * {@link GeneticRecombinator} implementation for {@link CellularAutomatonGeneticData}.
 */
public class CellularAutomatonGeneticRecombinator implements GeneticRecombinator {

  private final GeneticRecombinator geneticRecombinator;
  private final CellularAutomatonSimulationModel model;
  private final CellularAutomatonMutator mutator;

  public CellularAutomatonGeneticRecombinator(GeneticRecombinator geneticRecombinator,
      CellularAutomatonSimulationModel model) {
    this(geneticRecombinator, null, model);
  }

  public CellularAutomatonGeneticRecombinator(GeneticRecombinator geneticRecombinator,
      CellularAutomatonMutator mutator, CellularAutomatonSimulationModel model) {
    this.geneticRecombinator = geneticRecombinator;
    this.mutator = mutator;
    this.model = model;
  }

  @Override
  public GeneticData recombine(GeneticData parent1Data, GeneticData parent2Data,
      RandomGenerator random) {
    CellularAutomatonGeneticData childData = new CellularAutomatonGeneticData(
        geneticRecombinator.recombine(parent1Data, parent2Data, random), model);

    if (mutator != null) {
      mutator.mutate(childData, random);
    }

    return childData;
  }
}
