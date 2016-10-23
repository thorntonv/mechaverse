package org.mechaverse.simulation.ant.core.module;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.AntSimulationState;
import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;

/**
 * An {@link AntSimulationModule} that records simulation replay data. This class records the
 * initial model when the state is set and the seed at the beginning of each iteration. During
 * replay the recorded seed is set at the beginning of each iteration so that the random number
 * generator can be used by modules that do not need to store replay data. Such modules must be
 * executed before modules that will cause the random number generator to become out of sync.
 * Typically this module should be executed before other modules.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class ReplayRecorderModule implements AntSimulationModule {

  private ByteArrayOutputStream seedDataByteOut;
  private DataOutputStream seedDataOut;

  @Override
  public void setState(AntSimulationState state, CellEnvironment env, EntityManager entityManager) {
    // Store the initial simulation model.
    state.getReplayDataStore().put(ReplayModule.INITIAL_MODEL_KEY,
        state.get(AntSimulationState.MODEL_KEY));
    seedDataByteOut = new ByteArrayOutputStream(64*1024);
    seedDataOut = new DataOutputStream(seedDataByteOut);
  }

  @Override
  public void updateState(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager) {
    // Store the seed data.
    state.getReplayDataStore().put(
        ReplayModule.RANDOM_SEED_DATA_KEY, seedDataByteOut.toByteArray());
  }

  @Override
  public void onAddEntity(Entity entity, AntSimulationState state) {}

  @Override
  public void onRemoveEntity(Entity entity, AntSimulationState state) {}

  @Override
  public void beforeUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {

    // Write the iteration seed.
    try {
      seedDataOut.writeLong(Long.parseLong(state.getModel().getSeed()));
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
