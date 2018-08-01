package org.mechaverse.gwt.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("service")
public interface SimulationGwtRpcService extends RemoteService {

  void loadState(String simulationId, String instanceId, long iteration)
      throws Exception;

  String getStateImage() throws Exception;

  void step();
}
