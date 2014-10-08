package org.mechaverse.simulation.ant.core.module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Nest;
import org.mechaverse.simulation.ant.core.AntSimulationUtil;
import org.mechaverse.simulation.ant.core.Cell;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;
import org.mechaverse.simulation.common.genetic.CutAndSpliceCrossoverGeneticRecombinator;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.genetic.GeneticRecombinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * An environment simulation module that maintains a target ant population size.
 *
 * @author Vance Thornton
 */
public class AntReproductionModule implements AntSimulationModule {

  /**
   * Calculates the fitness of a set of ants.
   */
  public static interface AntFitnessCalculator {

    EnumeratedDistribution<Ant> getAntFitnessDistribution(Set<Ant> ants, RandomGenerator random);
  }

  /**
   * An ant fitness calculator that calculates ant fitness based on age and energy. Age is given a
   * weight of 70% and energy is weighted 30%. The following formula is used:
   * <p>
   * <code>.7 * age/ageSum + .3 * energy/energySum</code>
   */
  public static class SimpleAntFitnessCalculator implements AntFitnessCalculator {

    // TODO(thorntonv) Separate this into its own class.

    @Override
    public EnumeratedDistribution<Ant> getAntFitnessDistribution(
        Set<Ant> ants, RandomGenerator random) {
      Preconditions.checkState(ants.size() > 0);

      double ageSum = 0;
      double energySum = 0;
      for (Ant ant : ants) {
        ageSum += ant.getAge();
        energySum += ant.getEnergy();
      }

      List<Pair<Ant, Double>> pmf = new ArrayList<>();
      for (Ant ant : ants) {
        if (ageSum != 0 && energySum != 0) {
          double fitness = .7 * ant.getAge() / ageSum + .3 * ant.getEnergy() / energySum;
          pmf.add(new Pair<Ant, Double>(ant, fitness));
        } else {
          pmf.add(new Pair<Ant, Double>(ant, 1.0D / ants.size()));
        }
      }

      return new EnumeratedDistribution<Ant>(random, pmf);
    }
  }

  // TODO(thorntonv): Support multiple nests and nests of different types.

  private static final Logger logger = LoggerFactory.getLogger(AntReproductionModule.class);

  @Value("#{properties['antMaxCount']}") private int antMaxCount;
  @Value("#{properties['antInitialEnergy']}") private int antInitialEnergy;

  private final Set<Ant> ants = new LinkedHashSet<>();
  private Nest nest;
  private final GeneticRecombinator geneticRecombinator;
  private final AntFitnessCalculator fitnessCalculator = new SimpleAntFitnessCalculator();

  public AntReproductionModule() {
    this(new CutAndSpliceCrossoverGeneticRecombinator());
  }

  public AntReproductionModule(GeneticRecombinator geneticRecombinator) {
    this.geneticRecombinator = geneticRecombinator;
  }

  @Override
  public void beforeUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {
    if (ants.size() < antMaxCount && nest != null) {
      Cell cell = env.getCell(nest);
      if (cell.getEntity(EntityType.ANT) == null) {
        Ant ant = ants.size() >= 2 ?
            generateAnt(state, random) : generateRandomAnt(state, random);
        cell.setEntity(ant, EntityType.ANT);
        entityManager.addEntity(ant);
      }
    }
  }

  @Override
  public void beforePerformAction(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}

  @Override
  public void afterUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}

  public Ant generateRandomAnt(AntSimulationState state, RandomGenerator random) {
    Ant ant = new Ant();
    ant.setId(new UUID(random.nextLong(), random.nextLong()).toString());
    ant.setDirection(AntSimulationUtil.randomDirection(random));
    ant.setEnergy(antInitialEnergy);
    ant.setMaxEnergy(antInitialEnergy);
    return ant;
  }

  public Ant generateAnt(AntSimulationState state, RandomGenerator random) {
    Ant ant = generateRandomAnt(state, random);

    EnumeratedDistribution<Ant> antFitnessDistribution =
        fitnessCalculator.getAntFitnessDistribution(ants, random);

    Ant parent1 = antFitnessDistribution.sample();
    Ant parent2 = antFitnessDistribution.sample();

    // Ensure that parent1 != parent2.
    while (ants.size() > 1 && parent2 == parent1) {
      parent2 = antFitnessDistribution.sample();
    }

    if(state.containsEntityKey(parent1, GeneticDataStore.KEY)
        && state.containsEntityKey(parent2, GeneticDataStore.KEY)) {
      try {
      // Get the parents genetic information.
      GeneticDataStore parent1GeneticDataStore =
          GeneticDataStore.deserialize(state.getEntityValue(parent1, GeneticDataStore.KEY));
      GeneticDataStore parent2GeneticDataStore =
          GeneticDataStore.deserialize(state.getEntityValue(parent2, GeneticDataStore.KEY));

      GeneticDataStore childGeneticDataStore = new GeneticDataStore();
      for (String key : parent1GeneticDataStore.keySet()) {
        GeneticData parent1Data = parent1GeneticDataStore.get(key);
        GeneticData parent2Data = parent2GeneticDataStore.get(key);
        GeneticData childData = geneticRecombinator.recombine(parent1Data, parent2Data, random);
        childGeneticDataStore.put(key, childData);
      }
      state.putEntityValue(ant, GeneticDataStore.KEY, childGeneticDataStore.serialize());

      logger.debug("Generated child ant {} with parents {} and {}",
          ant.getId(), parent1.getId(), parent2.getId());
      } catch(IOException ex) {
        logger.warn("Error generating ant", ex);
      }
    }

    return ant;
  }

  @Override
  public void onAddEntity(Entity entity) {
    if (entity instanceof Ant) {
      ants.add((Ant) entity);
    } else if (entity instanceof Nest) {
      nest = (Nest) entity;
    }
  }

  @Override
  public void onRemoveEntity(Entity entity) {
    if (entity instanceof Ant) {
      ants.remove(entity);
    } else if (entity instanceof Nest) {
      if (entity == nest) {
        nest = null;
      }
    }
  }

  @VisibleForTesting
  void setAntMaxCount(int antMaxCount) {
    this.antMaxCount = antMaxCount;
  }
}
