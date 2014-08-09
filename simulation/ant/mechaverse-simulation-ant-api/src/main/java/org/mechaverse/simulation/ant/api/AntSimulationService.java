package org.mechaverse.simulation.ant.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlRootElement;

import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.SimulationModel;

/**
 * Interface for the mechaverse ant simulation service.
 */
@Path("/")
public interface AntSimulationService {

  /**
   * The status of the simulation.
   */
  @XmlRootElement(name = "SimulationStatus")
  public static enum SimulationStatus {
    RUNNING, STOPPED, STEPPING
  }

  /**
   * @return the current state of the simulation
   * @throws Exception if an error occurs while processing the request
   */
  @GET
  @Path("/simulation/state/current")
  @Produces("application/xml")
  SimulationModel getCurrentState() throws Exception;

  /**
   * Sets the current simulation state.
   * @param model the new state
   * @throws Exception if an error occurs while processing the request
   */
  @POST
  @Path("/simulation/state/current")
  @Consumes("application/xml")
  void setCurrentState(SimulationModel model) throws Exception;

  /**
   * Sets the execution status of the simulation.
   * @param status the new execution status of the simulation.
   * @throws Exception if an error occurs while processing the request
   */
  @POST
  @Path("/simulation/status")
  @Consumes("application/xml")
  void setStatus(SimulationStatus status) throws Exception;

  /**
   * Step the simulation.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @POST
  @Path("/simulation/step")
  void step() throws Exception;

  /**
   * @return true if the simulation is running, false otherwise
   * @throws Exception if an error occurs while processing the request
   */
  @GET
  @Path("/simulation/running")
  @Produces("application/xml")
  boolean isRunning() throws Exception;

  /**
   * Loads the state with the given key.
   *
   * @param key the key of the state to load
   * @return the loaded state
   * @throws Exception if an error occurs while processing the request
   */
  @GET
  @Path("/simulation/state/{key}")
  @Produces("application/xml")
  SimulationModel loadState(@PathParam("key") String key) throws Exception;

  /**
   * Saves the given state with the given key.
   *
   * @param key the key of the state to save
   * @param state the simulation state
   * @throws Exception if an error occurs while processing the request
   */
  @POST
  @Path("/simulation/state/{key}")
  @Consumes("application/xml")
  void saveState(@PathParam("key") String key, SimulationModel state) throws Exception;

  /**
   * Attempts to transfer the specified entity from the source environment to the specified target
   * environment.
   *
   * @param sourceEnvironmentId the id of the source environment
   * @param targetEnvironmentId the id of the target environment
   * @throws Exception if an error occurs while processing the request
   */
  @POST
  @Path("/simulation/state/current/entity")
  @Consumes("application/xml")
  void transferEntityIn(@QueryParam("sourceEnvironmentId") String sourceEnvironmentId,
      @DefaultValue("") @QueryParam("targetEnvironmentId") String targetEnvironmentId,
      Entity entity) throws Exception;

  /**
   * Registers a peer node with this node.
   *
   * @param nodeId the peer node id
   * @throws Exception if an error occurs while processing the request
   */
  @POST
  @Path("/node/{nodeId}")
  @Consumes("application/xml")
  void registerPeerNode(@PathParam("nodeId") String nodeId) throws Exception;

  /**
   * Unregisters a peer node from this node.
   *
   * @param nodeId the peer node id
   * @throws Exception if an error occurs while processing the request
   */
  @DELETE
  @Path("/node/{nodeId}")
  void unregisterPeerNode(@PathParam("nodeId") String nodeId) throws Exception;

  /**
   * @return a string with device information
   * @throws Exception if an error occurs while processing the request
   */
  @GET
  @Path("/simulation/deviceInfo")
  @Produces("application/xml")
  String getDeviceInfo();
}
