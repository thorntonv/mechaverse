package org.mechaverse.simulation.common.simple;

import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.mechaverse.simulation.common.AbstractEntity;
import org.mechaverse.simulation.common.SimulationLogger;
import org.mechaverse.simulation.common.genetic.selection.SelectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import gnu.trove.map.TObjectDoubleMap;

/**
 * A simple {@link SimulationLogger} that logs the best and average entity fitness.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 *
 * @param <E> the entity type
 * @param <M> the simulation model type
 */
public class SimpleSimulationLogger<E extends AbstractEntity, M> implements SimulationLogger<E, M> {

  private static final Logger logger = LoggerFactory.getLogger(SimpleSimulationLogger.class);

  private final PrintWriter results;
  private final Function<E, Double> entityFitnessFunction;
  private int iterationsPerLog = 1;
  private boolean minimize = false;
  private Pair<E, Double> overallBestEntity;

  public SimpleSimulationLogger(PrintWriter results, Function<E, Double> entityFitnessFunction) {
    super();
    this.results = results;
    this.entityFitnessFunction = entityFitnessFunction;
  }

  @Override
  public void log(long iteration, M model, List<E> entities) {
    if (iteration % iterationsPerLog != 0) {
      return;
    }

    TObjectDoubleMap<E> entityFitnessMap =
        SelectionUtil.buildEntityFitnessMap(entities, entityFitnessFunction);
    entities = SelectionUtil.getEntitiesSortedByFitness(entityFitnessMap, minimize);

    E bestEntity = entities.get(0);
    double bestFitness = entityFitnessMap.get(bestEntity);

    double fitnessSum = 0;
    for (E entity : entities) {
      fitnessSum += entityFitnessFunction.apply(entity);
    }

    if (bestEntity != null) {
      if (overallBestEntity == null || (!minimize && bestFitness > overallBestEntity.getValue())
          || (minimize && bestFitness < overallBestEntity.getValue())) {
        overallBestEntity = new Pair<>(bestEntity, bestFitness);
      }

      double averageFitness = fitnessSum / entities.size();
      String result = String.format("%d,%f,%f", iteration, bestFitness, averageFitness);
      logger.info("Iteration {}: {}, best: {}", iteration, result, overallBestEntity.getValue());
      logger.info(bestEntity.toString());
      results.println(result);
      results.flush();
    }
  }

  @Override
  public boolean getMinimize() {
    return minimize;
  }

  @Override
  public void setMinimize(boolean minimize) {
    this.minimize = minimize;
  }

  @Override
  public Pair<E, Double> getOverallBestEntity() {
    return overallBestEntity;
  }
}
