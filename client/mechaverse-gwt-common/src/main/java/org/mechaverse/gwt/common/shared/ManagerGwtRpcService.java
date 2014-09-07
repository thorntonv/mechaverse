package org.mechaverse.gwt.common.shared;

import java.util.List;

import org.mechaverse.service.manager.api.model.SimulationConfig;
import org.mechaverse.service.manager.api.model.SimulationInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * A GWT RPC service for accessing the manager interface.
 *
 * @author Vance Thornton
 */
@RemoteServiceRelativePath("manager")
public interface ManagerGwtRpcService extends RemoteService {

  public List<SimulationInfo> getSimulationInfo() throws Exception;
  public SimulationInfo createSimulation(String name) throws Exception;
  public void setSimulationActive(String simulationId, boolean active);
  public SimulationInfo getSimulationInfo(String simulationId) throws Exception;
  public void updateSimulationConfig(SimulationConfig config) throws Exception;
  public void deleteSimulation(String simulationId) throws Exception;
}
