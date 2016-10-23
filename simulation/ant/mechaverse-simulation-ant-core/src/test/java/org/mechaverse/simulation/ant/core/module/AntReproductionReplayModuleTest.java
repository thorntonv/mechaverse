package org.mechaverse.simulation.ant.core.module;

import static org.mechaverse.simulation.ant.core.AntSimulationTestUtil.assertEntitiesEqual;
import static org.mechaverse.simulation.ant.core.AntSimulationTestUtil.newEntity;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.core.AntSimulationState;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.Entity;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.AntSimulationEnvironmentGenerator;
import org.mechaverse.simulation.ant.core.AntSimulationImpl;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;
import org.mechaverse.simulation.ant.core.entity.EntityDataOutputStream;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link AntReproductionReplayModule}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AntReproductionReplayModuleTest {

  private static final Entity ANT1 =
      newEntity(EntityType.ANT, "001", 10, 10, Direction.SOUTH, 75, 100);
  private static final Entity ANT2 =
      newEntity(EntityType.ANT, "002", 15, 15, Direction.EAST, 70, 100);

  @Mock EntityManager mockEntityManager;

  private AntSimulationState state;
  private CellEnvironment environment;
  private RandomGenerator random;
  private AntReproductionReplayModule antReproductionReplayModule;

  @Before
  public void setUp() {
    this.random = RandomUtil.newGenerator(ReplayRecorderModuleTest.class.hashCode());
    this.state = AntSimulationImpl.randomState(new AntSimulationEnvironmentGenerator(), random);
    this.environment = new CellEnvironment(state.getModel().getEnvironment());
    this.antReproductionReplayModule = new AntReproductionReplayModule();
  }

  @Test
  public void generate_singleAnt() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try(EntityDataOutputStream entityOut = new EntityDataOutputStream(byteOut)) {
      int iteration = 3;
      int antCount = 1;
      entityOut.writeLong(iteration);
      entityOut.writeInt(antCount);
      entityOut.writeEntity(ANT1);
    }

    state.getReplayDataStore().put(
        AntReproductionReplayModule.ANT_GENERATION_DATA_KEY, byteOut.toByteArray());
    antReproductionReplayModule.setState(state, environment, mockEntityManager);

    state.getModel().setIteration(1);
    antReproductionReplayModule.beforeUpdate(state, environment, mockEntityManager, random);
    verifyZeroInteractions(mockEntityManager);

    state.getModel().setIteration(3);
    antReproductionReplayModule.beforeUpdate(state, environment, mockEntityManager, random);
    ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
    verify(mockEntityManager).addEntity(entityCaptor.capture());
    assertEntitiesEqual(ANT1, entityCaptor.getValue());
  }

  @Test
  public void generate_multipleAnts() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try(EntityDataOutputStream entityOut = new EntityDataOutputStream(byteOut)) {
      int iteration = 3;
      int antCount = 2;
      entityOut.writeLong(iteration);
      entityOut.writeInt(antCount);
      entityOut.writeEntity(ANT1);
      entityOut.writeEntity(ANT2);
    }

    state.getReplayDataStore().put(
        AntReproductionReplayModule.ANT_GENERATION_DATA_KEY, byteOut.toByteArray());
    antReproductionReplayModule.setState(state, environment, mockEntityManager);

    state.getModel().setIteration(1);
    antReproductionReplayModule.beforeUpdate(state, environment, mockEntityManager, random);
    verifyZeroInteractions(mockEntityManager);

    state.getModel().setIteration(3);
    antReproductionReplayModule.beforeUpdate(state, environment, mockEntityManager, random);
    ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
    verify(mockEntityManager, times(2)).addEntity(entityCaptor.capture());
    assertEntitiesEqual(ANT1, entityCaptor.getAllValues().get(0));
    assertEntitiesEqual(ANT2, entityCaptor.getAllValues().get(1));
  }

  @Test
  public void generate_multipleIterations() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try(EntityDataOutputStream entityOut = new EntityDataOutputStream(byteOut)) {
      int iteration = 3;
      int antCount = 1;
      entityOut.writeLong(iteration);
      entityOut.writeInt(antCount);
      entityOut.writeEntity(ANT1);
      iteration = 7;
      entityOut.writeLong(iteration);
      entityOut.writeInt(antCount);
      entityOut.writeEntity(ANT2);
    }

    state.getReplayDataStore().put(
        AntReproductionReplayModule.ANT_GENERATION_DATA_KEY, byteOut.toByteArray());
    antReproductionReplayModule.setState(state, environment, mockEntityManager);

    ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);

    state.getModel().setIteration(3);
    antReproductionReplayModule.beforeUpdate(state, environment, mockEntityManager, random);
    verify(mockEntityManager).addEntity(entityCaptor.capture());
    assertEntitiesEqual(ANT1, entityCaptor.getValue());

    state.getModel().setIteration(5);
    antReproductionReplayModule.beforeUpdate(state, environment, mockEntityManager, random);
    verifyZeroInteractions(mockEntityManager);

    state.getModel().setIteration(7);
    antReproductionReplayModule.beforeUpdate(state, environment, mockEntityManager, random);
    verify(mockEntityManager, times(2)).addEntity(entityCaptor.capture());
    assertEntitiesEqual(ANT2, entityCaptor.getValue());
  }
}
