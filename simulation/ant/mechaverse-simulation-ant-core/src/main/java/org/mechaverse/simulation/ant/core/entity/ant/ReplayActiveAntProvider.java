package org.mechaverse.simulation.ant.core.entity.ant;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.AntSimulationState;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.entity.ActiveEntity;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.model.EntityModel;

import java.io.IOException;

/**
 * An {@link ActiveEntityProvider} implementation that provides an ant implementation that replays
 * recorded output data.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class ReplayActiveAntProvider implements ActiveEntityProvider {

  // TODO(thorntonv): Implement unit tests for these classes.
  
  public static final class ReplayActiveAnt implements ActiveEntity {

    private final ActiveAnt ant;

    public ReplayActiveAnt(Ant entity, ReplayAntBehavior antBehavior) {
      ant = new ActiveAnt(entity, antBehavior);
    }

    @Override
    public void updateInput(CellEnvironment env, RandomGenerator random) {
      // Not necessary during replay.
    }

    @Override
    public void performAction(CellEnvironment env, EntityManager entityManager, 
        RandomGenerator random) {
      ant.performAction(env, entityManager, random);
    }

    @Override
    public EntityModel getEntity() {
      return ant.getEntity();
    }

    @Override
    public EntityType getType() {
      return ant.getType();
    }

    @Override
    public void setState(AntSimulationState state) {
      ant.setState(state);
    }

    @Override
    public void updateState(AntSimulationState state) {}

    @Override
    public void onRemoveEntity() {}
  }

  public static class ReplayAntBehavior implements AntBehavior {

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

  @Override
  public ActiveEntity getActiveEntity(EntityModel entity) {
    return new ReplayActiveAnt((Ant) entity, new ReplayAntBehavior());
  }
}
