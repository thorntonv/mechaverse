package org.mechaverse.simulation.primordial.core.util;

import java.io.IOException;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.SimulationModelUtil;
import org.mechaverse.simulation.primordial.core.model.Barrier;
import org.mechaverse.simulation.primordial.core.model.Food;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

public class PrimordialSimulationModelUtil {

  private static final Class[] CLASSES_TO_BE_BOUND =
      new Class[] {PrimordialSimulationModel.class, PrimordialEnvironmentModel.class, PrimordialEntityModel.class,
              Barrier.class, Food.class};

  public static PrimordialSimulationModel deserialize(byte[] data) throws IOException {
    return SimulationModelUtil.deserialize(data, CLASSES_TO_BE_BOUND, PrimordialSimulationModel.class);
  }

  public static byte[] serialize(SimulationModel model) throws IOException {
    return SimulationModelUtil.serialize(model);
  }
}
