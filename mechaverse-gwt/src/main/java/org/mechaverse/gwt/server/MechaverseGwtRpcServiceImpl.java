package org.mechaverse.gwt.server;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.mechaverse.gwt.shared.MechaverseGwtRpcService;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.api.SimulationService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of {@link MechaverseGwtRpcService} which forwards requests to the REST service.
 */
public class MechaverseGwtRpcServiceImpl extends RemoteServiceServlet
    implements MechaverseGwtRpcService {

  private static final long serialVersionUID = 1349327476429682957L;

  // TODO(thorntonv): Inject this service.
  private SimulationService service = JAXRSClientFactory.create(
      "http://localhost:8080/mechaverse-simulation-ant-service", SimulationService.class);

  @Override
  public SimulationModel loadState(String simulationId, String instanceId, long iteration)
      throws Exception {
    byte[] randomState = service.generateRandomState();
    service.setState(0, randomState);
    return getModel();
  }

  @Override
  public void saveState(String simulationId, String instanceId, long iteration) {
    // TODO(thorntonv): Implement method.
  }

  @Override
  public SimulationModel getModel() throws Exception {
    byte[] modelData = service.getStateValue(0, AntSimulationState.MODEL_KEY);
    return AntSimulationState.deserializeModel(modelData);
  }

  @Override
  public void setModel(SimulationModel state) {
  }

  @Override
  public void step() throws Exception {
    service.step(0, 1);
  }
}
