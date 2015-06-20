package org.mechaverse.simulation.common.simple;

import java.io.PrintWriter;
import java.util.List;

import org.mechaverse.simulation.common.AbstractEntity;
import org.mechaverse.simulation.common.SimulationLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

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
  private int iterationsPerLog = 10;
  private double overallBestFitness = 0;
  
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
    
    double bestFitness = 0;
    E bestEntity = null;

    double fitnessSum = 0;
    int fitEntityCount = 0;
    for (E entity : entities) {
      double fitness = entityFitnessFunction.apply(entity);
      
      if (fitness > 0) {
        if (fitness > bestFitness) {
          bestEntity = entity;
          bestFitness = fitness;
        }
        fitnessSum += fitness;
        fitEntityCount++;
      }
    }
    
    if (bestEntity != null) {
      if(bestFitness > overallBestFitness) {
        overallBestFitness = bestFitness;
      }
      double averageFitness = fitnessSum / fitEntityCount;
      String result = String.format("%d,%f,%f", iteration, bestFitness, averageFitness);
      logger.info("Iteration {}: {}, best: {}", iteration, result, overallBestFitness);

      results.println(result);
      results.flush();
    }
  }
}
