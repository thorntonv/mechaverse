package org.mechaverse.gwt.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.mechaverse.gwt.shared.MechaverseGwtRpcService;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.api.Simulation;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.collect.ImmutableList;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of {@link MechaverseGwtRpcService} which forwards requests to the REST service.
 */
public class MechaverseGwtRpcServiceImpl extends RemoteServiceServlet
    implements MechaverseGwtRpcService {

  private static final long serialVersionUID = 1349327476429682957L;

  private static final String SIMULATION_SERVICE_KEY = "simulation-service";
  private static final String STORAGE_SERVICE_KEY = "storage-service";

  @Override
  public SimulationModel loadState(String simulationId, String instanceId, long iteration)
      throws Exception {
    try {
      Simulation service = getSimulation();
      synchronized (service) {
        InputStream stateIn = getStorageService().getState(simulationId, instanceId, iteration);
        service.setState(SimulationDataStore.deserialize(IOUtils.readBytesFromStream(stateIn)));
        return getModel();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }
  }

  @Override
  public void saveState(String simulationId, String instanceId, long iteration) {
    // TODO(thorntonv): Implement method.
  }

  @Override
  public SimulationModel getModel() throws Exception {
    Simulation service = getSimulation();
    synchronized (service) {
      byte[] modelData = service.getState().get(AntSimulationState.MODEL_KEY);
      return AntSimulationState.deserializeModel(new GZIPInputStream(
          new ByteArrayInputStream(modelData)));
    }
  }

  @Override
  public void setModel(SimulationModel state) {
    // TODO(thorntonv): Implement method.
  }

  @Override
  public void step() throws Exception {
    Simulation service = getSimulation();
    synchronized (service) {
      service.step(1);
    }
  }

  private Simulation getSimulation() {
    HttpServletRequest request = this.getThreadLocalRequest();
    Simulation service = (Simulation) request.getSession(true)
        .getAttribute(SIMULATION_SERVICE_KEY);
    if (service == null) {
      try (ClassPathXmlApplicationContext ctx =
          new ClassPathXmlApplicationContext("simulation-context.xml")) {
        service = ctx.getBean(Simulation.class);
      }
      request.getSession().setAttribute(SIMULATION_SERVICE_KEY, service);
    }
    return service;
  }

  private MechaverseStorageService getStorageService() {
    HttpServletRequest request = this.getThreadLocalRequest();
    MechaverseStorageService service = (MechaverseStorageService) request.getSession(true)
        .getAttribute(STORAGE_SERVICE_KEY);
    if (service == null) {
      service = JAXRSClientFactory.create(
        "http://mechaverse.org:8080/mechaverse-storage-service", MechaverseStorageService.class,
        ImmutableList.of(new JacksonJaxbJsonProvider()));
      request.getSession().setAttribute(STORAGE_SERVICE_KEY, service);
    }
    return service;
  }
}
