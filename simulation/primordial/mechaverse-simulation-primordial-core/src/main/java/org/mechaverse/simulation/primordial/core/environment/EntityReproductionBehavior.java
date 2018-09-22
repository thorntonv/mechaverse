package org.mechaverse.simulation.primordial.core.environment;

import com.google.common.annotations.VisibleForTesting;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.genetic.BitMutator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.EntityFitnessDistribution;
import org.mechaverse.simulation.common.util.SimulationUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    int newRandomEntityCount = 0;
    int newCloneEntityCount = 0;
    while (entities.size() < entityMaxCount && cnt < entityMaxCount) {
      if (fitnessDistribution == null) {
        fitnessDistribution = buildEntityFitnessDistribution(state);
      }
      int row = random.nextInt(envModel.getHeight());
      int col = random.nextInt(envModel.getWidth());

      if (envModel.isValidCell(row, col) && !envModel.hasEntity(row, col)) {
        PrimordialEntityModel selectedEntity = fitnessDistribution.selectEntity(random);
        PrimordialEntityModel clone = generateRandomEntity(state, random);
        clone.setX(col);
        clone.setY(row);
        envModel.addEntity(clone);
        env.addEntity(clone);

        if (selectedEntity != null) {
          // Copy genetic data to new entity.
          int[] geneticData = envModel.getEntityGeneticData(selectedEntity);
          if (geneticData != null) {
            int[] cloneData = geneticData.clone();
            bitMutator.mutate(cloneData, random);
            envModel.setEntityGeneticData(clone, cloneData);
          }
          newCloneEntityCount++;
        } else {
          newRandomEntityCount++;
        }
      }
      cnt++;
    }
    if (newCloneEntityCount > 0 || newRandomEntityCount > 0) {
      logger.info(String.format("Created %d new clones and %d new random entities",
          newCloneEntityCount, newRandomEntityCount));
    }
  }

  private EntityFitnessDistribution<PrimordialEntityModel, EntityType> buildEntityFitnessDistribution(
      PrimordialSimulationModel state) {
    PrimordialEntityModel[] models = new PrimordialEntityModel[entities.size()];
    int idx = 0;
    for (PrimordialEntityModel entityModel : entities) {
      models[idx++] = entityModel;
    }
    return new EntityFitnessDistribution<>(models, entity -> (double) (
        state.getIteration() - entity.getCreatedIteration() + entity.getEnergy()));
  }

  private PrimordialEntityModel generateRandomEntity(SimulationModel state,
      RandomGenerator random) {
    PrimordialEntityModel entity = new PrimordialEntityModel();
    entity.setId(new UUID(random.nextLong(), random.nextLong()).toString());
    entity.setDirection(SimulationUtil.randomDirection(random));
    entity.setEnergy(entityInitialEnergy);
    entity.setMaxEnergy(entityInitialEnergy);
    entity.setCreatedIteration(state.getIteration());
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
