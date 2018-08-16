package org.mechaverse.simulation.primordial.core.environment;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator;
import org.mechaverse.simulation.common.genetic.BitMutator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.EntityFitnessDistribution;
import org.mechaverse.simulation.common.util.SimulationUtil;
import org.mechaverse.simulation.primordial.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * An environment simulation module that maintains a target entity population size.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
@SuppressWarnings("unused")
public class EntityReproductionBehavior extends PrimordialEnvironmentBehavior {

    private static final Logger logger = LoggerFactory.getLogger(EntityReproductionBehavior.class);

    private int entityMaxCount;
    private int entityInitialEnergy;
    private int entityMinReproductiveAge;
    private BitMutator bitMutator;

    private final Set<PrimordialEntityModel> entities = new LinkedHashSet<>();

    public EntityReproductionBehavior() {
    }

    @Override
    public void setState(final PrimordialSimulationModel state,
                         final Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment) {
        super.setState(state, environment);
        entityMaxCount = state.getEntityMaxCountPerEnvironment();
        entityInitialEnergy = state.getEntityInitialEnergy();
        entityMinReproductiveAge = state.getEntityMinReproductiveAge();
        bitMutator = new BitMutator(state.getMutationRate());
    }

    @Override
    public void beforeUpdate(PrimordialSimulationModel state,
                             Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> env,
                             RandomGenerator random) {
        final PrimordialEnvironmentModel envModel = env.getModel();
        int cnt = 0;
        EntityFitnessDistribution<PrimordialEntityModel, EntityType> fitnessDistribution = null;
        while (entities.size() < entityMaxCount && cnt < entityMaxCount) {
            if (fitnessDistribution == null) {
                fitnessDistribution = buildEntityFitnessDistribution();
            }
            int row = random.nextInt(envModel.getHeight());
            int col = random.nextInt(envModel.getWidth());

            if (envModel.hasCell(row, col)) {
                PrimordialCellModel cell = envModel.getCell(row, col);
                if (cell.getEntity(EntityType.ENTITY) == null) {
                    PrimordialEntityModel selectedEntity = fitnessDistribution.selectEntity(random);
                    PrimordialEntityModel clone = generateRandomEntity(state, random);
                    cell.setEntity(clone);
                    env.addEntity(clone);

                    if(selectedEntity != null) {
                        // Copy genetic data to new entity.
                        for (String key : CellularAutomatonGeneticDataGenerator.KEY_SET) {
                            byte[] cloneData = selectedEntity.getData(key).clone();
                            bitMutator.mutate(cloneData, random);
                            clone.putData(key, cloneData);
                        }
                    }
                }
            }
            cnt++;
        }
    }

    private EntityFitnessDistribution<PrimordialEntityModel, EntityType> buildEntityFitnessDistribution() {
        PrimordialEntityModel[] models = new PrimordialEntityModel[entities.size()];
        int idx = 0;
        for (PrimordialEntityModel entityModel : entities) {
            models[idx++] = entityModel;
        }
        return new EntityFitnessDistribution<>(models,
                entity -> entity.getAge() >= entityMinReproductiveAge ? entity.getAge() : 0.0);
    }

    private PrimordialEntityModel generateRandomEntity(SimulationModel state,
                                                       RandomGenerator random) {
        PrimordialEntityModel entity = new PrimordialEntityModel();
        entity.setId(new UUID(random.nextLong(), random.nextLong()).toString());
        entity.setDirection(SimulationUtil.randomDirection(random));
        entity.setEnergy(entityInitialEnergy);
        entity.setMaxEnergy(entityInitialEnergy);
        return entity;
    }

    @Override
    public void onAddEntity(EntityModel<EntityType> entity, PrimordialSimulationModel state,
                            PrimordialEnvironmentModel envModel) {
        if (entity instanceof PrimordialEntityModel) {
            entities.add((PrimordialEntityModel) entity);
        }
    }

    @Override
    public void onRemoveEntity(EntityModel<EntityType> entity, PrimordialSimulationModel state,
                               PrimordialEnvironmentModel envModel) {
        if (entity instanceof PrimordialEntityModel) {
            entities.remove(entity);
        }
    }

    @VisibleForTesting
    void setEntityMaxCount(int entityMaxCount) {
        this.entityMaxCount = entityMaxCount;
    }
}
