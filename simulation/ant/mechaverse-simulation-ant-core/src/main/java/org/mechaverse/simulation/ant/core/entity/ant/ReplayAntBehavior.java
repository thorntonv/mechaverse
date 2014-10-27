package org.mechaverse.simulation.ant.core.entity.ant;

import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt.AntBehavior;

/**
 * An {@link AntBehavior} implementation that replays recorded ant output data.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class ReplayAntBehavior implements AntBehavior {

  private Ant entity;
  private AntOutputDataInputStream outputReplayDataInputStream;

  @Override
  public void setEntity(Ant entity) {
    this.entity = entity;
  }

  @Override
  public void setInput(AntInput input, RandomGenerator random) {}

  @Override
  public AntOutput getOutput(RandomGenerator random) {
    try {
      return outputReplayDataInputStream.readAntOutput();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onRemoveEntity() {}

  @Override
  public void setState(AntSimulationState state) {
    outputReplayDataInputStream = new AntOutputDataInputStream(
        state.getEntityReplayDataStore(entity).get(ActiveAnt.OUTPUT_REPLAY_DATA_KEY));
  }

  @Override
  public void updateState(AntSimulationState state) {}
}
