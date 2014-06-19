package org.mechaverse.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.mechaverse.simulation.ant.api.AntSimulationService;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.SimulationState;
import org.mechaverse.simulation.ant.core.AntSimulationImpl;

/**
 * Implementation of {@link MechaverseService}.
 *
 * @author thorntonv@mechaverse.org
 */
public class MechaverseServiceImpl implements AntSimulationService {

  private final AntSimulationImpl simulation = new AntSimulationImpl();

  @Override
  public SimulationState getCurrentState() throws Exception {
    return simulation.getState();
  }

  @Override
  public void setCurrentState(SimulationState state) throws Exception {
    simulation.setState(state);
  }

  @Override
  public void setStatus(SimulationStatus status) throws Exception {
  }

  @Override
  public void step() throws Exception {
    simulation.step();
  }

  @Override
  public boolean isRunning() throws Exception {
    return true;
  }

  @Override
  public SimulationState loadState(String key) throws Exception {
    InputStream in = new GZIPInputStream(new FileInputStream(getSimulationFilename(key)));
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(SimulationState.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (SimulationState) jaxbUnmarshaller.unmarshal(in);
    } finally {
      in.close();
    }
  }

  @Override
  public void saveState(String key, SimulationState state) throws Exception {
    OutputStream out = new GZIPOutputStream(new FileOutputStream(getSimulationFilename(key)));
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(SimulationState.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.marshal(state, out);
    } finally {
      out.close();
    }
  }

  @Override
  public void transferEntityIn(
      String sourceEnvironmentId, String targetEnvironmentId, Entity entity) throws Exception {
    // TODO(thorntonv): Implement transferEntityIn method.
  }

  @Override
  public void registerPeerNode(String nodeId) throws Exception {
    // TODO(thorntonv): Implement registerPeerNode method.
  }

  @Override
  public void unregisterPeerNode(String nodeId) throws Exception {
    // TODO(thorntonv): Implement unregisterPeerNode method.
  }

  @Override
  public String getDeviceInfo() {
    return "";
  }

  /**
   * @return the file name for the simulation with the given key
   */
  private String getSimulationFilename(String key) {
    return "simulation/" + key;
  }
}
