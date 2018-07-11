package org.mechaverse.simulation.ant.core.module;

import static org.junit.Assert.assertEquals;
import static org.mechaverse.simulation.ant.core.AntSimulationTestUtil.assertModelsEqual;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.core.util.AntSimulationModelUtil;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.ant.core.AntSimulationEnvironmentGenerator;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.util.RandomUtil;

/**
 * Unit test for {@link ReplayModule}.
 */
public class ReplayModuleTest {

  private AntSimulationState state;
  private CellEnvironment environment;
  private RandomGenerator random;
  private ReplayModule replayModule;
  private EntityManager entityManager = null;

  @Before
  public void setUp() {
    this.random = RandomUtil.newGenerator(ReplayRecorderModuleTest.class.hashCode());
    this.state = AntSimulationImpl.randomState(new AntSimulationEnvironmentGenerator(), random);
    this.environment = new CellEnvironment(state.getModel().getEnvironment());
    this.replayModule = new ReplayModule();
  }

  @Test
  public void replayInitialState() throws IOException {
    SimulationModel expectedModel = AntSimulationImpl.randomState().getModel();
    byte[] expectedModelBytes = AntSimulationModelUtil.serialize(expectedModel);
    state.getReplayDataStore().put(ReplayModule.INITIAL_MODEL_KEY, expectedModelBytes);
    state.getReplayDataStore().put(ReplayModule.RANDOM_SEED_DATA_KEY, new byte[0]);
    replayModule.setState(state, environment, entityManager);
    assertModelsEqual(expectedModel, state.getModel());
  }

  @Test
  public void replayRandomSeeds() throws IOException {
    state.getReplayDataStore().put(ReplayModule.INITIAL_MODEL_KEY,
        state.get(AntSimulationState.MODEL_KEY));
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    DataOutputStream seedDataOut = new DataOutputStream(byteOut);
    seedDataOut.writeLong(100);
    seedDataOut.writeLong(101);
    seedDataOut.writeLong(102);
    state.getReplayDataStore().put(ReplayModule.RANDOM_SEED_DATA_KEY, byteOut.toByteArray());
    replayModule.setState(state, environment, entityManager);

    replayModule.beforeUpdate(state, environment, entityManager, random);
    assertEquals(100, Long.parseLong(state.getModel().getSeed()));
    replayModule.beforeUpdate(state, environment, entityManager, random);
    assertEquals(101, Long.parseLong(state.getModel().getSeed()));
    replayModule.beforeUpdate(state, environment, entityManager, random);
    assertEquals(102, Long.parseLong(state.getModel().getSeed()));
  }
}
