package org.mechaverse.manager.service.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for the mechaverse storage service.
 */
public interface MechaverseStorageService {

  /**
   * Returns the state of the specified instance.
   *
   * @throws IOException if an error occurs while processing the request
   */
  InputStream getState(String simulationId, String instanceId, long iteration) throws IOException;

  /**
   * Sets the state of the specified instance.
   *
   * @throws IOException if an error occurs while processing the request
   */
  void setState(String simulationId, String instanceId, long iteration, InputStream stateInput)
      throws IOException;

  /**
   * Deletes a simulation.
   */
  void deleteSimulation(String simulationId) throws IOException;

  /**
   * Deletes a simulation instance.
   */
  void deleteInstance(String simulationId, String instanceId) throws IOException;
}
