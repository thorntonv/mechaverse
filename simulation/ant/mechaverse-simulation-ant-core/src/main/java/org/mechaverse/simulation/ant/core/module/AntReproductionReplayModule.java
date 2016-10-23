package org.mechaverse.simulation.ant.core.module;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.AntSimulationState;
import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.Cell;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;
import org.mechaverse.simulation.ant.core.entity.EntityDataInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AntSimulationModule} that replays ant generation data recorded by the
 * {@link ReplayRecorderModule}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class AntReproductionReplayModule implements AntSimulationModule {

  public static final String ANT_GENERATION_DATA_KEY = "antGeneration";

  private static final Logger logger = LoggerFactory.getLogger(AntReproductionReplayModule.class);

  private long nextIteration;
  private List<Entity> nextAnts;
  private EntityDataInputStream antGenerationReplayDataIn;

  @Override
  public void onAddEntity(Entity entity, AntSimulationState state) {}

  @Override
  public void onRemoveEntity(Entity entity, AntSimulationState state) {}

  @Override
  public void setState(AntSimulationState state, CellEnvironment env, EntityManager entityManager) {
    byte[] generationData = state.getReplayDataStore().get(ANT_GENERATION_DATA_KEY);
    antGenerationReplayDataIn = new EntityDataInputStream(new ByteArrayInputStream(generationData));
  }

  @Override
  public void updateState(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager) {}

  @Override
  public void beforeUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {
    if (antGenerationReplayDataIn != null) {
      try {
        if (nextAnts == null) {
          nextIteration = antGenerationReplayDataIn.readLong();
          int antCount = antGenerationReplayDataIn.readInt();
          nextAnts = new ArrayList<>(antCount);
          for (int cnt = 1; cnt <= antCount; cnt++) {
            nextAnts.add(antGenerationReplayDataIn.readEntity());
          }
        }
        if (state.getIteration() == nextIteration) {
          for (Entity entity : nextAnts) {
            logger.debug("Generated ant {}", entity.getId());
            Cell cell = env.getCell(entity.getY(), entity.getX());
            cell.setEntity(entity, EntityType.ANT);
            entityManager.addEntity(entity);
          }
          nextAnts = null;
        }
      } catch (EOFException ex) {
        // All ants have been generated.
        antGenerationReplayDataIn = null;
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  @Override
  public void beforePerformAction(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}

  @Override
  public void afterUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}
}
