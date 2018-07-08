package org.mechaverse.gwt.common.server;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.mechaverse.gwt.common.shared.ManagerGwtRpcService;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.manager.api.model.SimulationConfig;
import org.mechaverse.service.manager.api.model.SimulationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of the {@link ManagerGwtRpcService}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class ManagerGwtRpcServiceImpl extends RemoteServiceServlet implements ManagerGwtRpcService {

  private static final long serialVersionUID = -1273742074908104295L;

  @Autowired private MechaverseManager manager;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    WebApplicationContext context =
        WebApplicationContextUtils.getWebApplicationContext(getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Override
  public List<SimulationInfo> getSimulationInfo() {
    return manager.getSimulationInfo();
  }

  @Override
  public SimulationInfo createSimulation(String name) {
    return manager.createSimulation(name);
  }

  @Override
  public void setSimulationActive(String simulationId, boolean active) {
    manager.setSimulationActive(simulationId, active);
  }

  @Override
  public SimulationInfo getSimulationInfo(String simulationId) {
    return manager.getSimulationInfo(simulationId);
  }

  @Override
  public void updateSimulationConfig(SimulationConfig config) {
    manager.updateSimulationConfig(config);
  }

  @Override
  public void deleteSimulation(String simulationId) throws Exception {
    manager.deleteSimulation(simulationId);
  }
}
