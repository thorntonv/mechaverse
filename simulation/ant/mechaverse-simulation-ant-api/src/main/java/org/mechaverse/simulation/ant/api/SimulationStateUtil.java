package org.mechaverse.simulation.ant.api;

import java.util.Collection;
import java.util.List;

import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationState;

import com.google.common.collect.Lists;

/**
 * Utility methods for {@link SimulationState}.
 *
 * @author thorntonv@mechaverse.org
 */
public final class SimulationStateUtil {

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
  public static Collection<Environment> getEnvironments(SimulationState state) {
    List<Environment> environments = Lists.newArrayList();
    environments.add(state.getEnvironment());
    environments.addAll(state.getSubEnvironments());
    return environments;
  }

  private SimulationStateUtil() {}
}
