package org.mechaverse.gwt.common.server;

import java.net.URI;
import java.util.List;

import org.jboss.resteasy.client.ClientRequestFactory;
import org.mechaverse.gwt.common.shared.ManagerGwtRpcService;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.SimulationConfig;
import org.mechaverse.service.manager.api.model.SimulationInfo;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of the {@link ManagerGwtRpcService}.
 *
 * @author Vance Thornton
 */
public class ManagerGwtRpcServiceImpl extends RemoteServiceServlet implements ManagerGwtRpcService {

  private static final long serialVersionUID = -1273742074908104295L;

  // TODO(thorntonv): Inject this service.
  private final MechaverseManager manager = new ClientRequestFactory(URI.create(
      "http://localhost:8080/mechaverse-manager"))
        .createProxy(MechaverseManager.class);

  @Override
  public List<SimulationInfo> getSimulationInfo() throws Exception {
    return manager.getSimulationInfo();
  }

  @Override
  public SimulationInfo createSimulation(String name) throws Exception {
    return manager.createSimulation(name);
  }

  @Override
  public void setSimulationActive(String simulationId, boolean active) {
    manager.setSimulationActive(simulationId, active);
  }

  @Override
  public SimulationInfo getSimulationInfo(String simulationId) throws Exception {
    return manager.getSimulationInfo(simulationId);
  }

  @Override
  public void updateSimulationConfig(SimulationConfig config) throws Exception {
    manager.updateSimulationConfig(config);
  }

  @Override
  public void deleteSimulation(String simulationId) throws Exception {
    manager.deleteSimulation(simulationId);
  }
}
