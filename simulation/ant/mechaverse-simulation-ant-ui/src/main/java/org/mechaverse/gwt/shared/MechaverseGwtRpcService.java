package org.mechaverse.gwt.shared;

import org.mechaverse.simulation.ant.api.model.SimulationModel;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("service")
public interface MechaverseGwtRpcService extends RemoteService {

  SimulationModel loadState(String simulationId, String instanceId, long iteration)
      throws Exception;

  void saveState(String simulationId, String instanceId, long iteration) throws Exception;

  SimulationModel getModel() throws Exception;
  void setModel(SimulationModel model) throws Exception;
  void step() throws Exception;
}
