package org.mechaverse.simulation.ant.core.util;

import java.io.IOException;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.Barrier;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.Conduit;
import org.mechaverse.simulation.ant.core.model.Dirt;
import org.mechaverse.simulation.ant.core.model.Food;
import org.mechaverse.simulation.ant.core.model.Nest;
import org.mechaverse.simulation.ant.core.model.Pheromone;
import org.mechaverse.simulation.ant.core.model.Rock;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.SimulationModelUtil;

public class AntSimulationModelUtil {

  private static final Class[] CLASSES_TO_BE_BOUND =
      new Class[] {SimulationModel.class, CellEnvironment.class, Ant.class, Barrier.class,
          Conduit.class, Dirt.class, Food.class, Nest.class, Pheromone.class, Rock.class};

  public static AntSimulationModel deserialize(byte[] data) throws IOException {
    return SimulationModelUtil.deserialize(data, CLASSES_TO_BE_BOUND, AntSimulationModel.class);
  }

  public static byte[] serialize(SimulationModel model) throws IOException {
    return SimulationModelUtil.serialize(model);
  }
}
