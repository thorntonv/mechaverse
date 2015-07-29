package org.mechaverse.simulation.bool.majority;

import com.google.common.base.Function;

/**
 * A fitness function for the {@link MajorityFunctionSimulation}. Entities that are younger than 75
 * iterations are assigned a fitness of zero. After that the fitness is the percentage of iterations
 * where the entity gave a correct output out of the total number of iterations the entity has
 * lived.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class MajorityFunctionFitnessCalculator
    implements Function<MajorityFunctionEntity, Double> {

  public static final MajorityFunctionFitnessCalculator INSTANCE =
      new MajorityFunctionFitnessCalculator();

  @Override
  public Double apply(MajorityFunctionEntity entity) {
    return entity.getNumCorrect() / (double) entity.getAge();
  }
}
