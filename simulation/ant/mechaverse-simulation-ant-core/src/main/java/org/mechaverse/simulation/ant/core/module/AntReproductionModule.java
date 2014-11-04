package org.mechaverse.simulation.ant.core.module;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.mechaverse.simulation.ant.core.entity.EntityDataOutputStream;
import org.mechaverse.simulation.common.genetic.CutAndSpliceCrossoverGeneticRecombinator;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.genetic.GeneticRecombinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * An environment simulation module that maintains a target ant population size.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class AntReproductionModule implements AntSimulationModule {

  /**
   * Calculates the fitness of a set of ants.
   */
  public static interface AntFitnessCalculator {

    EnumeratedDistribution<Ant> getAntFitnessDistribution(
        Collection<Ant> ants, RandomGenerator random);
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
        Collection<Ant> ants, RandomGenerator random) {
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
  @Value("#{properties['antMinReproductiveAge']}") private int antMinReproductiveAge;

  // TODO(thorntonv): Determine if this should be a simulation property.
  private int maxGeneratedAntNestDistance = 15;

  private final Set<Ant> ants = new LinkedHashSet<>();
  private Nest nest;
  private final GeneticRecombinator geneticRecombinator;
  private final AntFitnessCalculator fitnessCalculator = new SimpleAntFitnessCalculator();

  private ByteArrayOutputStream antGenerationReplayDataByteOut;
  private EntityDataOutputStream antGenerationReplayDataOut;


  public AntReproductionModule() {
    this(new CutAndSpliceCrossoverGeneticRecombinator());
  }

  public AntReproductionModule(GeneticRecombinator geneticRecombinator) {
    this.geneticRecombinator = geneticRecombinator;
  }

  @Override
  public void setState(AntSimulationState state, CellEnvironment env, EntityManager entityManager) {
    antGenerationReplayDataByteOut = new ByteArrayOutputStream(64 * 1024);
    antGenerationReplayDataOut = new EntityDataOutputStream(antGenerationReplayDataByteOut);
  }

  @Override
  public void updateState(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager) {
    // Store the ant generation data.
    state.getReplayDataStore().put(AntReproductionReplayModule.ANT_GENERATION_DATA_KEY,
        antGenerationReplayDataByteOut.toByteArray());
  }

  @Override
  public void beforeUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {
    if (ants.size() < antMaxCount && nest != null) {
      Cell nestCell = env.getCell(nest);
      int row = random.nextInt(maxGeneratedAntNestDistance * 2) - maxGeneratedAntNestDistance
          + nestCell.getRow();
      int col = random.nextInt(maxGeneratedAntNestDistance * 2) - maxGeneratedAntNestDistance
          + nestCell.getColumn();

      if(env.hasCell(row, col)) {
        Cell cell = env.getCell(row, col);
        if (cell.getEntity(EntityType.ANT) == null) {
          Ant ant = generateAnt(state, random);
          cell.setEntity(ant, EntityType.ANT);
          entityManager.addEntity(ant);
          writeAntGenerationReplayData(ImmutableList.of(ant), state);
        }
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

    Collection<Ant> reproductiveAnts = getReproductiveAnts();
    if (reproductiveAnts.size() < 2) {
      logger.debug("Generated ant {}", ant.getId());
      return ant;
    }

    EnumeratedDistribution<Ant> antFitnessDistribution =
        fitnessCalculator.getAntFitnessDistribution(reproductiveAnts, random);

    Ant parent1 = antFitnessDistribution.sample();
    Ant parent2 = antFitnessDistribution.sample();

    // Ensure that parent1 != parent2.
    while (ants.size() > 1 && parent2 == parent1) {
      parent2 = antFitnessDistribution.sample();
    }

    // Get the parents genetic information.
    GeneticDataStore parent1GeneticDataStore = state.getEntityGeneticDataStore(parent1);
    GeneticDataStore parent2GeneticDataStore = state.getEntityGeneticDataStore(parent2);

    GeneticDataStore childGeneticDataStore = state.getEntityGeneticDataStore(ant);
    for (String key : parent1GeneticDataStore.keySet()) {
      GeneticData parent1GeneticData = parent1GeneticDataStore.get(key);
      GeneticData parent2GeneticData = parent2GeneticDataStore.get(key);
      GeneticData childData = geneticRecombinator.recombine(
          parent1GeneticData, parent2GeneticData, random);
      childGeneticDataStore.put(key, childData);
    }

    logger.debug("Generated child ant {} with parents {} and {}",
        ant.getId(), parent1.getId(), parent2.getId());

    return ant;
  }

  @Override
  public void onAddEntity(Entity entity, AntSimulationState state) {
    if (entity instanceof Ant) {
      ants.add((Ant) entity);
    } else if (entity instanceof Nest) {
      nest = (Nest) entity;
    }
  }

  @Override
  public void onRemoveEntity(Entity entity, AntSimulationState state) {
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

  private Collection<Ant> getReproductiveAnts() {
    List<Ant> reproductiveAnts = new ArrayList<>();
    for (Ant ant : ants) {
      if (ant.getAge() >= antMinReproductiveAge) {
        reproductiveAnts.add(ant);
      }
    }
    return reproductiveAnts;
  }

  private void writeAntGenerationReplayData(List<Ant> ants, AntSimulationState state) {
    // Write ant generation replay data.
    if (antGenerationReplayDataOut != null) {
      try {
        antGenerationReplayDataOut.writeLong(state.getIteration());
        // Write entity count.
        antGenerationReplayDataOut.writeInt(ants.size());
        for(Ant ant : ants) {
          antGenerationReplayDataOut.writeEntity(ant);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
