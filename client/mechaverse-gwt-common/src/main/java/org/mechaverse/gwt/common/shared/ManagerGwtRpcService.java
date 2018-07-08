package org.mechaverse.gwt.common.shared;

import java.util.List;

import org.mechaverse.service.manager.api.model.SimulationConfig;
import org.mechaverse.service.manager.api.model.SimulationInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * A GWT RPC service for accessing the manager interface.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
@RemoteServiceRelativePath("manager")
public interface ManagerGwtRpcService extends RemoteService {

  List<SimulationInfo> getSimulationInfo();
  SimulationInfo createSimulation(String name);
  void setSimulationActive(String simulationId, boolean active);
  SimulationInfo getSimulationInfo(String simulationId);
  void updateSimulationConfig(SimulationConfig config);
  void deleteSimulation(String simulationId) throws Exception;
}
