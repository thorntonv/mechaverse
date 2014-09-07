package org.mechaverse.simulation.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Interface for a mechaverse simulation service.
 */
@Path("/")
public interface SimulationService {

  /**
   * Returns the number of instances that can be accessed using this service.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @GET
  @Path("/instance/count")
  @Produces(MediaType.APPLICATION_JSON)
  int getInstanceCount() throws Exception;

  /**
   * Returns the state of the specified instance.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @GET
  @Path("/instance/{instanceIdx}/state")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  byte[] getState(@PathParam("instanceIdx") int instanceIdx) throws Exception;

  /**
   * Returns the state of the specified instance.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @GET
  @Path("/instance/{instanceIdx}/state")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  byte[] getStateValue(@PathParam("instanceIdx") int instanceIdx, @QueryParam("key") String key)
      throws Exception;

  /**
   * Sets the state of the specified instance.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @POST
  @Path("/instance/{instanceIdx}/state")
  @Consumes(MediaType.WILDCARD)
  void setState(@PathParam("instanceIdx") int instanceIdx, byte[] state) throws Exception;

  /**
   * Sets the state of the specified instance.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @POST
  @Path("/instance/{instanceIdx}/state")
  @Consumes(MediaType.WILDCARD)
  void setStateValue(@PathParam("instanceIdx") int instanceIdx, @QueryParam("key") String key,
      byte[] value) throws Exception;

  /**
   * Generates a random state.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @GET
  @Path("/random/state")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  byte[] generateRandomState() throws Exception;

  /**
   * Step the simulation of the specified instance.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @POST
  @Path("/instance/{instanceIdx}/step")
  @Consumes(MediaType.APPLICATION_JSON)
  void step(@PathParam("instanceIdx") int instanceIdx, int stepCount) throws Exception;

  /**
   * Returns a string with information about the device used by the specified simulation instance.
   *
   * @throws Exception if an error occurs while processing the request
   */
  @GET
  @Path("/instance/{instanceIdx}/deviceInfo")
  @Produces(MediaType.APPLICATION_JSON)
  String getDeviceInfo(@PathParam("instanceIdx") int instanceIdx);
}
