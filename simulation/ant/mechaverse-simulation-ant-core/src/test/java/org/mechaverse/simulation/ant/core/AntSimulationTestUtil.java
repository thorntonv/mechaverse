package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.mechaverse.simulation.ant.core.entity.EntityUtil;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.util.AntSimulationModelUtil;

/**
 * Common ant simulation test utility methods.
 */
public class AntSimulationTestUtil {

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

    assertArrayEquals(AntSimulationModelUtil.serialize(expected), AntSimulationModelUtil.serialize(actual));
  }
}
