package org.mechaverse.simulation.primordial.core.environment;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.genetic.BitMutator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.SimulationUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
  private int entityMaxEnergy;
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
    entityMaxEnergy = state.getEntityMaxEnergy();
    entityMinReproductiveAge = state.getEntityMinReproductiveAge();
    bitMutator = new BitMutator(state.getMutationRate());
  }

  @Override
  public void beforeUpdate(PrimordialSimulationModel state,
      Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> env,
      RandomGenerator random) {
    if(entities.size() >= entityMaxCount) {
      return;
    }
    logger.info("Performing reproduction");
    Random shuffleRand = new Random(random.nextLong());
    final PrimordialEnvironmentModel envModel = env.getModel();
    int cnt = 0;
    int newRandomEntityCount = 0;
    int newCloneEntityCount = 0;
    List<PrimordialEntityModel> reproductiveEntities = buildReproductiveEntityList(state);

    Collections.shuffle(reproductiveEntities, shuffleRand);
    List<int[]> positionDeltas = new ArrayList<>(SimulationUtil.POSITION_DELTAS);
    int maxTries = (entityMaxCount - entities.size()) / 200;
    maxTries = Math.max(1, maxTries);

    if(state.getIteration() < 10) {
      maxTries = entityMaxCount;
    }
    while (entities.size() < entityMaxCount && cnt < maxTries) {
      int row, col;
      PrimordialEntityModel selectedEntity = null;
      if (!reproductiveEntities.isEmpty() && random.nextFloat() > .05) {
        selectedEntity = reproductiveEntities.get(random.nextInt(reproductiveEntities.size()));
        row = selectedEntity.getY() + positionDeltas.get(random.nextInt(positionDeltas.size()))[0];
        col = selectedEntity.getX() + positionDeltas.get(random.nextInt(positionDeltas.size()))[1];
      } else {
        row = random.nextInt(envModel.getHeight());
        col = random.nextInt(envModel.getWidth());
      }

      clone(selectedEntity, row, col, state, env, envModel, random);

      cnt++;
    }
  }

  private boolean clone(PrimordialEntityModel entity, int row, int col,
      PrimordialSimulationModel state,
      Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> env,
      PrimordialEnvironmentModel envModel, RandomGenerator random) {
    if (!envModel.isValidCell(row, col) || envModel.hasEntity(row, col)) {
      return false;
    }

    PrimordialEntityModel clone = generateRandomEntity(state, random);
    clone.setX(col);
    clone.setY(row);
    envModel.addEntity(clone);
    env.addEntity(clone);

    if (entity != null) {
      if (entity.getStrainId() == 0) {
        entity.setStrainId(random.nextLong());
      }

      clone.setStrainId(entity.getStrainId());
      clone.setEnergy(entity.getEnergy() * 3 / 4);

      // Copy genetic data to new entity.
      int[] geneticData = envModel.getEntityGeneticData(entity);
      if (geneticData != null) {
        int[] cloneData = geneticData.clone();
        bitMutator.mutate(cloneData, random);
        envModel.setEntityGeneticData(clone, cloneData);
      }
    }
    return true;
  }

  private List<PrimordialEntityModel> buildReproductiveEntityList(PrimordialSimulationModel state) {
    List<PrimordialEntityModel> retVal = new ArrayList<>();
    for (PrimordialEntityModel entity : entities) {
      if(entity.getEnergy() > 0) {
        long age = state.getIteration() - entity.getCreatedIteration();
        if (age >= entityMinReproductiveAge) {
          retVal.add(entity);
        }
      }
    }
    return retVal;
  }

  private PrimordialEntityModel generateRandomEntity(SimulationModel state,
      RandomGenerator random) {
    PrimordialEntityModel entity = new PrimordialEntityModel();
    entity.setId(new UUID(random.nextLong(), random.nextLong()).toString());
    entity.setDirection(SimulationUtil.randomDirection(random));
    entity.setEnergy(entityInitialEnergy);
    entity.setMaxEnergy(entityMaxEnergy);
    entity.setCreatedIteration(state.getIteration());
    entity.setStrainId(0);
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
