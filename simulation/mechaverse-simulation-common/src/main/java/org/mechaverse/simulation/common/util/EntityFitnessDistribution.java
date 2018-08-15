package org.mechaverse.simulation.common.util;

import com.google.common.base.Preconditions;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;

import java.util.Arrays;
import java.util.function.Function;

public class EntityFitnessDistribution<ENT_MODEL extends EntityModel<ENT_TYPE>, ENT_TYPE extends Enum<ENT_TYPE>> {

    private ENT_MODEL[] entities;
    private double[] cumulativeFitness;

    public EntityFitnessDistribution(ENT_MODEL[] entities, Function<ENT_MODEL, Double> fitnessFunction) {
        this.entities = Preconditions.checkNotNull(entities);
        Preconditions.checkNotNull(fitnessFunction);
        cumulativeFitness = new double[entities.length];
        double total = 0;
        for (int idx = 0; idx < entities.length; idx++) {
            if (entities[idx] != null) {
                Double fitness = fitnessFunction.apply(entities[idx]);
                total += fitness != null ? fitness : 0.0;
            }
            cumulativeFitness[idx] = total;
        }
        if(total > 0) {
            for (int idx = 0; idx < cumulativeFitness.length; idx++) {
                cumulativeFitness[idx] /= total;
            }
        }
    }

    public ENT_MODEL selectEntity(RandomGenerator random) {
        if (cumulativeFitness.length == 0 || cumulativeFitness[cumulativeFitness.length - 1] == 0) {
            return null;
        }
        int idx = Arrays.binarySearch(cumulativeFitness, random.nextDouble());
        if (idx < 0) {
            idx = -idx - 1;
        }
        return idx < entities.length ? entities[idx] : null;
    }
}
