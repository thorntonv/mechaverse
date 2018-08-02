package org.mechaverse.gwt.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.mechaverse.gwt.shared.SimulationGwtRpcService;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.ui.SimulationImageProvider;
import org.mechaverse.simulation.common.ui.SimulationRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of {@link SimulationGwtRpcService} which forwards requests to the REST service.
 */
@Service
public class SimulationGwtRpcServiceImpl extends RemoteServiceServlet
        implements SimulationGwtRpcService, HttpSessionListener {

    private static final long serialVersionUID = 1349327476429682957L;

    private static final String SIMULATION_SERVICE_KEY = "simulation-service";
    private static final String SIMULATION_CONTEXT_KEY = "simulation-context";
    private static final String STORAGE_SERVICE_KEY = "storage-service";

    private static final Logger logger = LoggerFactory.getLogger(SimulationGwtRpcServiceImpl.class);

    @Autowired
    private ObjectFactory<MechaverseStorageService> storageServiceClientFactory;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext context =
                WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        context.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Override
    public void loadState(String simulationId, String instanceId, long iteration)
            throws Exception {
        try (InputStream in = getStorageService().getState(simulationId, instanceId, iteration)) {
//            MemorySimulationDataStore.MemorySimulationDataStoreInputStream stateIn =
//                    new MemorySimulationDataStore.MemorySimulationDataStoreInputStream(in);
//            SimulationDataStore dataStore = stateIn.readDataStore();
//            String contextConfigName = new String(dataStore.get("contextConfigName"));
            Simulation simulation = createSimulation("ant-simulation-context.xml");
//            simulation.setStateData(dataStore.get("model"));
            simulation.setState(simulation.generateRandomState());
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    @Override
    public String getStateImage() throws Exception {
        Simulation simulation = getSimulation();
        SimulationImageProvider imageProvider = getSimulationImageProvider();
        if(simulation == null || imageProvider == null) {
            throw new Exception("Simulation not initialized");
        }
        SimulationRenderer renderer = new SimulationRenderer(imageProvider, imageProvider.getCellImageSize());
        SimulationModel simulationModel = simulation.getState();
        BufferedImage image = renderer.draw(simulationModel, simulationModel.getEnvironment());

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteOut);
        byteOut.flush();
        byte[] imageBytes = byteOut.toByteArray();
        byteOut.close();
        Base64.Encoder encoder = Base64.getEncoder();
        return "data:image/png;base64," + encoder.encodeToString(imageBytes);
    }

    @Override
    public void step() {
        Simulation service = getSimulation();
        synchronized (service) {
            service.step(1);
        }
    }

    private Simulation createSimulation(String contextConfigName) {
        HttpServletRequest request = this.getThreadLocalRequest();
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext(contextConfigName);
        Simulation simulation = ctx.getBean(Simulation.class);
        request.getSession().setAttribute(SIMULATION_SERVICE_KEY, simulation);
        request.getSession().setAttribute(SIMULATION_CONTEXT_KEY, ctx);
        return simulation;
    }

    private Simulation getSimulation() {
        HttpServletRequest request = this.getThreadLocalRequest();
        return (Simulation) request.getSession(true)
                .getAttribute(SIMULATION_SERVICE_KEY);
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

    private SimulationImageProvider getSimulationImageProvider() {
        HttpServletRequest request = this.getThreadLocalRequest();
        ClassPathXmlApplicationContext appContext = (ClassPathXmlApplicationContext) request.getSession(true)
                .getAttribute(SIMULATION_CONTEXT_KEY);
        if (appContext != null) {
            return appContext.getBean(SimulationImageProvider.class);
        }
        return null;
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {
    }

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
