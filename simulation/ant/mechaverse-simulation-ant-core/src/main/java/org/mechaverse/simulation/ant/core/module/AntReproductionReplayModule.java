package org.mechaverse.simulation.ant.core.module;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.AbstractAntEnvironmentBehavior;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.model.Cell;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.ant.core.entity.EntityDataInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link org.mechaverse.simulation.common.EnvironmentBehavior} that replays ant generation data
 * recorded by the {@link ReplayRecorderModule}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class AntReproductionReplayModule extends AbstractAntEnvironmentBehavior {

  public static final String ANT_GENERATION_DATA_KEY = "antGeneration";

  private static final Logger logger = LoggerFactory.getLogger(AntReproductionReplayModule.class);

  private long nextIteration;
  private List<EntityModel> nextAnts;
  private EntityDataInputStream antGenerationReplayDataIn;

  @Override
  public void setState(AntSimulationModel state, CellEnvironment env, EntityManager entityManager) {
    byte[] generationData = state.getReplayDataStore().get(ANT_GENERATION_DATA_KEY);
    antGenerationReplayDataIn = new EntityDataInputStream(new ByteArrayInputStream(generationData));
  }

  @Override
  public void beforeUpdate(AntSimulationModel state, CellEnvironment env,
      EntityManager<AntSimulationModel, EntityModel<EntityType>> entityManager, RandomGenerator random) {
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
          for (EntityModel entity : nextAnts) {
            logger.debug("Generated ant {}", entity.getId());
            Cell cell = env.getCell(entity.getY(), entity.getX());
            cell.setEntity(entity);
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
}
