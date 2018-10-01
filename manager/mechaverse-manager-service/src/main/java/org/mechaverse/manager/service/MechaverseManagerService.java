package org.mechaverse.manager.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.mechaverse.manager.service.model.SimulationConfig;
import org.mechaverse.manager.service.model.SimulationInfo;
import org.mechaverse.manager.service.model.Task;

/**
 * Interface for a mechaverse manager service.
 */
public interface MechaverseManagerService {

  /**
   * Returns a task to perform.
   */
  Task getTask(String clientId);

  /**
   * Used to submit the result of a task.
   */
  void submitResult(long taskId, InputStream resultDataInput) throws Exception;

  /**
   * Returns information about all simulations.
   */
  List<SimulationInfo> getSimulationInfo();

  /**
   * Creates a new simulation.
   */
  SimulationInfo createSimulation(String name);

  /**
   * Activates or deactivates a simulation.
   */
  void setSimulationActive(String simulationId, boolean active);

  /**
   * Returns information about a simulation.
   */
  SimulationInfo getSimulationInfo(String simulationId);

  /**
   * Get the state data for the given iteration of the simulation instance.
   */
  InputStream getState(String simulationId, String instanceId, long iteration) throws IOException;

  /**
   * Updates the configuration for a simulation.
   */
  void updateSimulationConfig(SimulationConfig config);

  /**
   * Deletes a simulation.
   */
  void deleteSimulation(String simulationId) throws Exception;
}
