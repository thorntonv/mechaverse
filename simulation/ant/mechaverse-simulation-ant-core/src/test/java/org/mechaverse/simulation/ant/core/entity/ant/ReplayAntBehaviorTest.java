package org.mechaverse.simulation.ant.core.entity.ant;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.core.AntSimulationTestUtil;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.ant.core.entity.ant.ReplayActiveAntProvider.ReplayAntBehavior;
import org.mechaverse.simulation.common.util.RandomUtil;

/**
 * Unit test for {@link ReplayAntBehavior}.
 */
public class ReplayAntBehaviorTest {

  private Ant entity;
  private AntBehavior behavior;
  private RandomGenerator random;

  @Before
  public void setUp() {
    entity = new Ant();
    entity.setId("001");
    behavior = new ReplayAntBehavior();
    behavior.setEntity(entity);
    random = RandomUtil.newGenerator(ReplayAntBehaviorTest.class.getName().hashCode());
  }

  @Test
  public void getOutput() throws IOException {
    AntSimulationState state = new AntSimulationState();

    // Generate replay data.
    List<AntOutput> expectedOutputs = new ArrayList<>();
    try (AntOutputDataOutputStream replayDataOut = new AntOutputDataOutputStream()) {
      for (int cnt = 1; cnt <= 100; cnt++) {
        AntOutput expectedOutput = AntSimulationTestUtil.randomAntOutput(random);
        replayDataOut.writeAntOutput(expectedOutput);
        expectedOutputs.add(expectedOutput);
      }
      replayDataOut.close();
      state.getEntityReplayDataStore(entity)
          .put(ActiveAnt.OUTPUT_REPLAY_DATA_KEY, replayDataOut.toByteArray());
    }

    behavior.setState(state);

    for(AntOutput expectedOutput : expectedOutputs) {
      assertArrayEquals(expectedOutput.getData(), behavior.getOutput(random).getData());
    }
  }
}
