package org.mechaverse.simulation.ant.core.module;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.AntSimulationState;
import org.mechaverse.simulation.ant.core.util.AntSimulationModelUtil;
import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;

/**
 * An {@link AntSimulationModule} that replays data recorded by the {@link ReplayRecorderModule}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class ReplayModule implements AntSimulationModule {

  public static final String INITIAL_MODEL_KEY = "initialModel";
  public static final String RANDOM_SEED_DATA_KEY = "randomSeeds";

  private DataInputStream seedDataIn;

  @Override
  public void onAddEntity(Entity entity, AntSimulationState state) {}

  @Override
  public void onRemoveEntity(Entity entity, AntSimulationState state) {}

  @Override
  public void setState(AntSimulationState state, CellEnvironment env, EntityManager entityManager) {
    try {
      // Load the model from the initial state.
      byte[] modelData = state.getReplayDataStore().get(INITIAL_MODEL_KEY);
      state.setModel(AntSimulationModelUtil.deserialize(
          new GZIPInputStream(new ByteArrayInputStream(modelData))));

      // Load the seed data.
      seedDataIn = new DataInputStream(new ByteArrayInputStream(
          state.getReplayDataStore().get(RANDOM_SEED_DATA_KEY)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void updateState(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager) {}

  @Override
  public void beforeUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {
    // Load the seed from the replay data.
    try {
      long seed = seedDataIn.readLong();
      random.setSeed(seed);
      state.getModel().setSeed(String.valueOf(seed));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void beforePerformAction(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}

  @Override
  public void afterUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}
}
