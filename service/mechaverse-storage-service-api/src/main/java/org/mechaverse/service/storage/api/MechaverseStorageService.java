package org.mechaverse.service.storage.api;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Interface for the mechaverse storage service.
 */
@Path("/")
public interface MechaverseStorageService {

  /**
   * Returns the state of the specified instance.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @GET
  @Path("/simulation/{simulationId}/{instanceId}/{iteration}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  InputStream getState(@PathParam("simulationId") String simulationId,
      @PathParam("instanceId") String instanceId,
      @PathParam("iteration") long iteration) throws IOException;

  /**
   * Returns the state of the specified instance.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @GET
  @Path("/simulation/{simulationId}/{instanceId}/{iteration}/{key}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  InputStream getStateValue(@PathParam("simulationId") String simulationId,
      @PathParam("instanceId") String instanceId,
      @PathParam("iteration") long iteration,
      @PathParam("key") String key) throws IOException;

  /**
   * Sets the state of the specified instance.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @POST
  @Path("/simulation/{simulationId}/{instanceId}/{iteration}")
  @Consumes("*/*")
  void setState(@PathParam("simulationId") String simulationId,
      @PathParam("instanceId") String instanceId,
      @PathParam("iteration") long iteration,
      InputStream stateInput) throws IOException;

  /**
   * Sets the state of the specified instance.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @POST
  @Path("/simulation/{simulationId}/{instanceId}/{iteration}/{key}")
  @Consumes("*/*")
  void setStateValue(@PathParam("simulationId") String simulationId,
      @PathParam("instanceId") String instanceId,
      @PathParam("iteration") long iteration,
      @PathParam("key") String key,
      InputStream valueInput) throws IOException;

  /**
   * Deletes a simulation.
   */
  @DELETE
  @Path("/simulation/{simulationId}")
  void deleteSimulation(@PathParam("simulationId") String simulationId) throws IOException;

  /**
   * Deletes a simulation instance.
   */
  @DELETE
  @Path("/simulation/{simulationId}/{instanceId}")
  void deleteInstance(@PathParam("simulationId") String simulationId,
      @PathParam("instanceId") String instanceId) throws IOException;
}
