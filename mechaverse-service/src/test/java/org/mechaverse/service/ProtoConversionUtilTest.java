package org.mechaverse.service;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mechaverse.api.proto.simulation.ant.AntSimulation;
import org.mechaverse.api.proto.simulation.ant.AntSimulation.Direction;
import org.mechaverse.api.proto.simulation.ant.AntSimulation.Environment;

import com.google.protobuf.ByteString;

/**
 * Unit test for {@link ProtoConversionUtil}.
 * 
 * @author thorntonv@mechaverse.org
 */
public class ProtoConversionUtilTest {

  private static final Environment.Builder ENVIRONMENT1 = newEnvironmentoBuilder(
    "environment1", 31, 32)
      .addAnt(newAntBuilder(newEntityStateBuilder("ant1", 2, 7, Direction.NORTH, 90, 100))
        .setCarriedEntityId("rock1"))
      .addAnt(newAntBuilder(newEntityStateBuilder("ant2", 2, 8, Direction.SOUTH, 99, 100)))
      .addBarrier(newBarrierBuilder(newEntityStateBuilder("barrier1", 1, 4)))
      .addConduit(newConduitBuilder("env2", newEntityStateBuilder("conduit1", 1, 1)))
      .addFood(newFoodBuilder(newEntityStateBuilder("food1", 2, 5)))
      .addPheromone(newPheromoneBuilder(newEntityStateBuilder("pheromone1", 2, 8))
        .setValue(3))
      .addRock(newRockBuilder(newEntityStateBuilder("rock1", 2, 7)));

  private static final Environment.Builder ENVIRONMENT2 = newEnvironmentoBuilder(
    "environment2", 31, 32)
      .addAnt(newAntBuilder(newEntityStateBuilder("ant1", 2, 7, Direction.EAST, 90, 100))
        .setCarriedEntityId("rock1"))
      .addAnt(newAntBuilder(newEntityStateBuilder("ant2", 2, 8, Direction.WEST, 99, 100)))
      .addBarrier(newBarrierBuilder(newEntityStateBuilder("barrier1", 1, 4)))
      .addFood(newFoodBuilder(newEntityStateBuilder("food1", 2, 5)))
      .addPheromone(newPheromoneBuilder(newEntityStateBuilder("pheromone1", 2, 8))
        .setValue(3))
      .addRock(newRockBuilder(newEntityStateBuilder("rock1", 2, 7)));

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testConversion() throws Exception {
    final AntSimulation.SimulationState stateProto =
        AntSimulation.SimulationState.newBuilder()
            .setId("simulation")
            .setEnvironment(ENVIRONMENT1)
            .addSubEnvironment(ENVIRONMENT2)
            .build();

    assertEquals(stateProto, ProtoConversionUtil.convert(ProtoConversionUtil.convert(stateProto)));
  }

  private static AntSimulation.Environment.Builder newEnvironmentoBuilder(
      String id, int width, int height) {
    return AntSimulation.Environment.newBuilder()
        .setId(id)
        .setWidth(width)
        .setHeight(height);
  }

  private static AntSimulation.Ant.Builder newAntBuilder(
      AntSimulation.EntityState.Builder entityStateBuilder) {
    return AntSimulation.Ant.newBuilder()
        .setEntityState(entityStateBuilder)
        .setData(ByteString.copyFrom("test".getBytes()))
        .setInputPositions(AntSimulation.Ant.InputPositions.newBuilder().setEnergyLevel(0))
        .setOutputPositions(AntSimulation.Ant.OutputPositions.newBuilder().setMove(1));
  }

  private static AntSimulation.Barrier.Builder newBarrierBuilder(
      AntSimulation.EntityState.Builder entityStateBuilder) {
    return AntSimulation.Barrier.newBuilder().setEntityState(entityStateBuilder);
  }

  private static AntSimulation.Conduit.Builder newConduitBuilder(String targetEnvironmentId,
      AntSimulation.EntityState.Builder entityStateBuilder) {
    return AntSimulation.Conduit.newBuilder()
        .setEntityState(entityStateBuilder)
        .setTargetEnvironmentId(targetEnvironmentId);
  }

  private static AntSimulation.Food.Builder newFoodBuilder(
      AntSimulation.EntityState.Builder entityStateBuilder) {
    return AntSimulation.Food.newBuilder().setEntityState(entityStateBuilder);
  }

  private static AntSimulation.Pheromone.Builder newPheromoneBuilder(
      AntSimulation.EntityState.Builder entityStateBuilder) {
    return AntSimulation.Pheromone.newBuilder().setEntityState(entityStateBuilder);
  }

  private static AntSimulation.Rock.Builder newRockBuilder(
      AntSimulation.EntityState.Builder entityStateBuilder) {
    return AntSimulation.Rock.newBuilder().setEntityState(entityStateBuilder);
  }

  private static AntSimulation.EntityState.Builder newEntityStateBuilder(String id, int x, int y) {
    return AntSimulation.EntityState.newBuilder().setId(id).setX(x).setY(y);
  }

  private static AntSimulation.EntityState.Builder newEntityStateBuilder(String id, int x, int y,
      AntSimulation.Direction direction, int energy, int maxEnergy) {
    return AntSimulation.EntityState.newBuilder()
        .setId(id)
        .setX(x)
        .setY(y)
        .setDirection(direction)
        .setEnergy(energy)
        .setMaxEnergy(maxEnergy);
  }
}
