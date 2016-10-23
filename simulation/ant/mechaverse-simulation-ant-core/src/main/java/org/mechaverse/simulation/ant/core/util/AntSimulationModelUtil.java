package org.mechaverse.simulation.ant.core.util;

import org.mechaverse.simulation.ant.core.model.*;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.SimulationModelUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AntSimulationModelUtil {

  private static final Class[] CLASSES_TO_BE_BOUND =
      new Class[] {SimulationModel.class, Ant.class, Barrier.class, Conduit.class, Dirt.class,
          Food.class, Nest.class, Pheromone.class, Rock.class};

  public static SimulationModel deserialize(InputStream in) throws IOException {
    return SimulationModelUtil.deserialize(in, CLASSES_TO_BE_BOUND);
  }

  public static void serialize(SimulationModel model, OutputStream out) throws IOException {
    SimulationModelUtil.serialize(model, CLASSES_TO_BE_BOUND, out);
  }

  public static byte[] serialize(SimulationModel model) throws IOException {
    return SimulationModelUtil.serialize(model, CLASSES_TO_BE_BOUND);
  }
}
