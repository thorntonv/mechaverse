package org.mechaverse.gwt.server;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.mechaverse.gwt.shared.MechaverseGwtRpcService;
import org.mechaverse.simulation.ant.api.AntSimulationService;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.SimulationModel;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of {@link MechaverseGwtRpcService} which forwards requests to the REST service.
 */
public class MechaverseGwtRpcServiceImpl extends RemoteServiceServlet
    implements MechaverseGwtRpcService {

  private static final long serialVersionUID = 1349327476429682957L;

  // TODO(thorntonv): Inject this service.
  private AntSimulationService service = JAXRSClientFactory.create(
      "http://localhost:8080/mechaverse-service", AntSimulationService.class);

  @Override
  public void setStatus(SimulationStatus status) throws Exception {
    service.setStatus(status);
  }

  @Override
  public void step() throws Exception {
    service.step();
  }

  @Override
  public boolean isRunning() throws Exception {
    return service.isRunning();
  }

  @Override
  public SimulationModel loadState(String key) throws Exception {
    return service.loadState(key);
  }

  @Override
  public void saveState(String key, SimulationModel state) throws Exception {
    service.saveState(key, state);
  }

  @Override
  public void transferEntityIn(
      String sourceEnvironmentId, String targetEnvironmentId, Entity entity) throws Exception {
    service.transferEntityIn(sourceEnvironmentId, targetEnvironmentId, entity);
  }

  @Override
  public void registerPeerNode(String nodeId) throws Exception {
    service.registerPeerNode(nodeId);
  }

  @Override
  public void unregisterPeerNode(String nodeId) throws Exception {
    service.unregisterPeerNode(nodeId);
  }

  @Override
  public SimulationModel getCurrentState() throws Exception {
    return service.getCurrentState();
  }

  @Override
  public void setCurrentState(SimulationModel state) throws Exception {
    service.setCurrentState(state);
  }

  @Override
  public String getDeviceInfo() {
    return service.getDeviceInfo();
  }
}
