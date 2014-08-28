package org.mechaverse.service.manager.api;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mechaverse.service.manager.api.model.SimulationConfig;
import org.mechaverse.service.manager.api.model.SimulationInfo;
import org.mechaverse.service.manager.api.model.Task;

/**
 * Interface for a mechaverse manager service.
 */
@Path("/")
public interface MechaverseManager {

  /**
   * Returns a task to perform.
   */
  @GET
  @Path("/task")
  @Produces(MediaType.APPLICATION_JSON)
  Task getTask() throws Exception;

  /**
   * Used to submit the result of a task.
   */
  @POST
  @Path("/task/{taskId}/result")
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  void submitResult(@PathParam("taskId") long taskId, InputStream resultDataInput) throws Exception;

  /**
   * Returns information about all simulations.
   */
  @GET
  @Path("/simulation")
  @Produces(MediaType.APPLICATION_JSON)
  List<SimulationInfo> getSimulationInfo() throws Exception;

  /**
   * Creates a new simulation.
   */
  @POST
  @Path("/simulation")
  @Produces(MediaType.APPLICATION_JSON)
  SimulationInfo createSimulation() throws Exception;

  /**
   * Returns information about a simulation.
   */
  @GET
  @Path("/simulation/{simulationId}")
  @Produces(MediaType.APPLICATION_JSON)
  SimulationInfo getSimulationInfo(@PathParam("simulationId") String simulationId) throws Exception;

  /**
   * Updates the configuration for a simulation.
   */
  @POST
  @Path("simulation/config")
  @Consumes(MediaType.APPLICATION_JSON)
  void updateSimulationConfig(SimulationConfig config) throws Exception;

  /**
   * Deletes a simulation.
   */
  @DELETE
  @Path("/simulation/{simulationId}")
  void deleteSimulation(@PathParam("simulationId") String simulationId) throws Exception;
}
