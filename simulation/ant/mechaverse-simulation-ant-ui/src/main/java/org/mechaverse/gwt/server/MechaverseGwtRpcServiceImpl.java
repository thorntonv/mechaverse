package org.mechaverse.gwt.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.mechaverse.gwt.shared.MechaverseGwtRpcService;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.SimulationModelUtil;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore.MemorySimulationDataStoreInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of {@link MechaverseGwtRpcService} which forwards requests to the REST service.
 */
public class MechaverseGwtRpcServiceImpl extends RemoteServiceServlet
    implements MechaverseGwtRpcService, HttpSessionListener {

  private static final long serialVersionUID = 1349327476429682957L;

  private static final String SIMULATION_SERVICE_KEY = "simulation-service";
  private static final String SIMULATION_CONTEXT_KEY = "simulation-context";
  private static final String STORAGE_SERVICE_KEY = "storage-service";

  private static final Logger logger = LoggerFactory.getLogger(MechaverseGwtRpcServiceImpl.class);

  @Autowired private ObjectFactory<MechaverseStorageService> storageServiceClientFactory;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    WebApplicationContext context =
        WebApplicationContextUtils.getWebApplicationContext(getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Override
  public SimulationModel loadState(String simulationId, String instanceId, long iteration)
      throws Exception {
    try {
      Simulation service = getSimulation();
      synchronized (service) {
        InputStream in = getStorageService().getState(simulationId, instanceId, iteration);
        MemorySimulationDataStoreInputStream stateIn = new MemorySimulationDataStoreInputStream(in);
        try {
          service.setState(stateIn.readDataStore());
          return getModel();
        } finally {
          in.close();
          stateIn.close();
        }
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
      return SimulationModelUtil.deserialize(new GZIPInputStream(
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
      ClassPathXmlApplicationContext ctx =
          new ClassPathXmlApplicationContext("simulation-context-replay.xml");
      service = ctx.getBean(Simulation.class);
      request.getSession().setAttribute(SIMULATION_SERVICE_KEY, service);
      request.getSession().setAttribute(SIMULATION_CONTEXT_KEY, ctx);
    }
    return service;
  }

  private MechaverseStorageService getStorageService() {
    HttpServletRequest request = this.getThreadLocalRequest();
    MechaverseStorageService service = (MechaverseStorageService) request.getSession(true)
        .getAttribute(STORAGE_SERVICE_KEY);
    if (service == null) {
      service = storageServiceClientFactory.getObject();
      request.getSession().setAttribute(STORAGE_SERVICE_KEY, service);
    }
    return service;
  }

  @Override
  public void sessionCreated(HttpSessionEvent event) {}

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    AbstractApplicationContext ctx =
        (AbstractApplicationContext) event.getSession().getAttribute(SIMULATION_CONTEXT_KEY);
    if (ctx != null) {
      logger.debug("sessionDestroyed");
      ctx.close();
    }
  }
}
