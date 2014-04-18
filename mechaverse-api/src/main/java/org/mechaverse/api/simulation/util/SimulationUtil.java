package org.mechaverse.api.simulation.util;

import java.util.List;

import org.mechaverse.api.model.simulation.ant.Environment;
import org.mechaverse.api.model.simulation.ant.SimulationState;

import com.google.common.collect.Lists;

/**
 * Utility methods for working with simulations.
 *  
 * @author thorntonv@mechaverse.org
 */
public final class SimulationUtil {

  /**
   * @return the environment with the given id from the given state
   */
  public static Environment getEnvironment(SimulationState state, String environmentId) {
    if (state.getEnvironment().getId().equals(environmentId)) {
      return state.getEnvironment();
    }
    for (Environment subEnvironment : state.getSubEnvironments()) {
      if (subEnvironment.getId().equals(environmentId)) {
        return subEnvironment;
      }
    }
    return null;
  }

  /**
   * @return a list of the environments in the given state
   */
  public static List<Environment> getEnvironments(SimulationState state) {
    List<Environment> environments = Lists.newArrayList();
    environments.add(state.getEnvironment());
    environments.addAll(state.getSubEnvironments());
    return environments;
  }

  private SimulationUtil() {}
}
