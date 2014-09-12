package org.mechaverse.gwt.server;

import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.mechaverse.gwt.shared.MechaverseGwtRpcService;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.ant.core.AntSimulationServiceImpl;
import org.mechaverse.simulation.api.SimulationService;

import com.google.common.collect.ImmutableList;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of {@link MechaverseGwtRpcService} which forwards requests to the REST service.
 */
public class MechaverseGwtRpcServiceImpl extends RemoteServiceServlet
    implements MechaverseGwtRpcService {

  private static final long serialVersionUID = 1349327476429682957L;

  private final SimulationService service = new AntSimulationServiceImpl();

  private final MechaverseStorageService storageService = JAXRSClientFactory.create(
    "http://mechaverse.org:8080/mechaverse-storage-service", MechaverseStorageService.class,
        ImmutableList.of(new JacksonJaxbJsonProvider()));

  @Override
  public SimulationModel loadState(String simulationId, String instanceId, long iteration)
      throws Exception {
    InputStream stateIn = storageService.getState(simulationId, instanceId, iteration);
    byte[] state = IOUtils.readBytesFromStream(stateIn);
    service.setState(0, state);
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
    // TODO(thorntonv): Implement method.
  }

  @Override
  public void step() throws Exception {
    service.step(0, 1);
  }
}
