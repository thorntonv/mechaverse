package org.mechaverse.gwt.server;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.mechaverse.api.model.simulation.ant.Entity;
import org.mechaverse.api.model.simulation.ant.SimulationState;
import org.mechaverse.api.service.MechaverseService;
import org.mechaverse.gwt.shared.MechaverseGwtRpcService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of {@link MechaverseGwtRpcService} which forwards requests to the REST service.
 * 
 * @author thorntonv@mechaverse.org
 */
public class MechaverseGwtRpcServiceImpl extends RemoteServiceServlet
    implements MechaverseGwtRpcService {

  private static final long serialVersionUID = 1349327476429682957L;

  // TODO(thorntonv): Inject this service.
  private MechaverseService service = JAXRSClientFactory.create(
      "http://localhost:8080/mechaverse-service", MechaverseService.class);

  @Override
  public void setStatus(SimulationStatus status) throws Exception {
    service.setStatus(status);
  }

  @Override
  public void step(int count) throws Exception {
    service.step(count);
  }

  @Override
  public boolean isRunning() throws Exception {
    return service.isRunning();
  }

  @Override
  public SimulationState loadState(String key) throws Exception {
    return service.loadState(key);
  }

  @Override
  public void saveState(String key, SimulationState state) throws Exception {
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
  public SimulationState getCurrentState() throws Exception {
    return service.getCurrentState();
  }

  @Override
  public void setCurrentState(SimulationState state) throws Exception {
    service.setCurrentState(state);
  }

  @Override
  public String getDeviceInfo() {
    return service.getDeviceInfo();
  }
}
