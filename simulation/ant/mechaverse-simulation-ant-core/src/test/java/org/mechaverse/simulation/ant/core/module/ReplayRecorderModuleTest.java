package org.mechaverse.simulation.ant.core.module;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mechaverse.simulation.ant.core.AntSimulationTestUtil.decompress;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.core.AntSimulationEnvironmentGenerator;
import org.mechaverse.simulation.ant.core.AntSimulationImpl;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;
import org.mechaverse.simulation.common.util.RandomUtil;

/**
 * Unit test for {@link ReplayRecorderModule}.
 */
public class ReplayRecorderModuleTest {

  private AntSimulationState state;
  private CellEnvironment environment;
  private RandomGenerator random;
  private ReplayRecorderModule replayRecorderModule;
  private EntityManager entityManager = null;

  @Before
  public void setUp() {
    this.random = RandomUtil.newGenerator(ReplayRecorderModuleTest.class.hashCode());
    this.state = AntSimulationImpl.randomState(new AntSimulationEnvironmentGenerator(), random);
    this.environment = new CellEnvironment(state.getModel().getEnvironment());
    this.replayRecorderModule = new ReplayRecorderModule();
  }

  @Test
  public void recordInitialState() throws IOException {
    byte[] expectedModel = state.get(AntSimulationState.MODEL_KEY);
    replayRecorderModule.setState(state, environment, entityManager);

    byte[] actualModel = state.getReplayDataStore().get(ReplayModule.INITIAL_MODEL_KEY);
    assertArrayEquals(decompress(expectedModel), decompress(actualModel));
  }

  @Test
  public void recordSeedData() throws IOException {
    state.getModel().setSeed("100");
    replayRecorderModule.setState(state, environment, entityManager);
    replayRecorderModule.beforeUpdate(state, environment, entityManager, random);
    state.getModel().setSeed("101");
    replayRecorderModule.beforeUpdate(state, environment, entityManager, random);
    state.getModel().setSeed("102");
    replayRecorderModule.beforeUpdate(state, environment, entityManager, random);

    replayRecorderModule.updateState(state, environment, entityManager);

    DataInputStream seedDataIn = new DataInputStream(new ByteArrayInputStream(
        state.getReplayDataStore().get(ReplayModule.RANDOM_SEED_DATA_KEY)));

    assertEquals(100, seedDataIn.readLong());
    assertEquals(101, seedDataIn.readLong());
    assertEquals(102, seedDataIn.readLong());
  }
}
