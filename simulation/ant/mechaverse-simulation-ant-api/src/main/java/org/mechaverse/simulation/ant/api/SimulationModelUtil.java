package org.mechaverse.simulation.ant.api;

import java.util.Collection;
import java.util.List;

import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationModel;

import com.google.common.collect.Lists;

/**
 * Utility methods for {@link SimulationModel}.
 */
public final class SimulationModelUtil {

  /**
   * @return the environment with the given id from the given state
   */
  public static Environment getEnvironment(SimulationModel simulationModel, String environmentId) {
    if (simulationModel.getEnvironment().getId().equals(environmentId)) {
      return simulationModel.getEnvironment();
    }
    for (Environment subEnvironment : simulationModel.getSubEnvironments()) {
      if (subEnvironment.getId().equals(environmentId)) {
        return subEnvironment;
      }
    }
    return null;
  }

  /**
   * @return a list of the environments in the given state
   */
  public static Collection<Environment> getEnvironments(SimulationModel simulationModel) {
    List<Environment> environments = Lists.newArrayList();
    environments.add(simulationModel.getEnvironment());
    environments.addAll(simulationModel.getSubEnvironments());
    return environments;
  }

  private SimulationModelUtil() {}
}
