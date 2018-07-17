package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertEquals;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.entity.EntityUtil;
import org.mechaverse.simulation.ant.core.entity.ant.AntOutput;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.ant.core.util.AntSimulationModelUtil;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;

/**
 * Common ant simulation test utility methods.
 */
public class AntSimulationTestUtil {

  public static EntityModel newEntity(EntityType entityType, String id, int x, int y,
      Direction direction, int energy, int maxEnergy) {
    EntityModel entity = EntityUtil.newEntity(entityType);
    entity.setId(id);
    entity.setX(x);
    entity.setY(y);
    entity.setDirection(direction);
    entity.setEnergy(energy);
    entity.setMaxEnergy(maxEnergy);
    return entity;
  }

  public static void assertModelsEqual(AntSimulationModel expected, AntSimulationModel actual)
      throws IOException {
    assertEquals(expected.getEnvironment().toString(), actual.getEnvironment().toString());

    // Sort the entities so that order will not cause the comparison to fail.
    for (CellEnvironment env : expected.getEnvironments()) {
      env.getEntities().sort(EntityUtil.ENTITY_ORDERING);
    }
    for (CellEnvironment env : actual.getEnvironments()) {
      env.getEntities().sort(EntityUtil.ENTITY_ORDERING);
    }

    ByteArrayOutputStream model1ByteOut = new ByteArrayOutputStream(16 * 1024);
    ByteArrayOutputStream model2ByteOut = new ByteArrayOutputStream(16 * 1024);

    AntSimulationModelUtil.serialize(expected, model1ByteOut);
    AntSimulationModelUtil.serialize(actual, model2ByteOut);

    assertEquals(model1ByteOut.toString(), model2ByteOut.toString());
  }

  public static void assertEntitiesEqual(EntityModel expected, EntityModel actual) {
    assertEquals(expected.getClass(), actual.getClass());
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getX(), actual.getX());
    assertEquals(expected.getY(), actual.getY());
    assertEquals(expected.getEnergy(), actual.getEnergy());
    assertEquals(expected.getMaxEnergy(), actual.getMaxEnergy());
  }

  public static AntOutput randomAntOutput(RandomGenerator random) {
    int[] antOutputData = new int[AntOutput.DATA_SIZE];
    for (int idx = 0; idx < antOutputData.length; idx++) {
      antOutputData[idx] = random.nextInt(Short.MAX_VALUE);
    }
    return new AntOutput(antOutputData);
  }

  public static byte[] decompress(byte[] data) throws IOException {
    return ByteStreams.toByteArray(new GZIPInputStream(new ByteArrayInputStream(data)));
  }
}
