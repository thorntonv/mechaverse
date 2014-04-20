package org.mechaverse.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.cxf.helpers.IOUtils;
import org.mechaverse.api.model.simulation.ant.Entity;
import org.mechaverse.api.model.simulation.ant.SimulationState;
import org.mechaverse.api.proto.simulation.ant.AntSimulation;
import org.mechaverse.api.service.MechaverseService;
import org.mechaverse.simulation.ant.Simulation;

/**
 * Implementation of {@link MechaverseService}.
 * 
 * @author thorntonv@mechaverse.org
 */
public class MechaverseServiceImpl implements MechaverseService {

  static {
    // Load the native simulation library.
    System.loadLibrary("mechaverse-simulation");
  }

  private static final Simulation simulation = new Simulation();

  @Override
  public SimulationState getCurrentState() throws Exception {
    int stateDataLength = simulation.getStateByteSize();
    byte[] stateData = new byte[stateDataLength];
    simulation.getState(stateData);
    AntSimulation.SimulationState stateProto = AntSimulation.SimulationState.parseFrom(stateData);
    return ProtoConversionUtil.convert(stateProto);
  }

  @Override
  public void setCurrentState(SimulationState state) throws Exception {
    simulation.setState(ProtoConversionUtil.convert(state).toByteArray());
  }

  @Override
  public void setStatus(SimulationStatus status) throws Exception {
    switch (status) {
      case RUNNING:
        simulation.start();
        break;
      case STEPPING:
        simulation.step();
        break;
      case STOPPED:
        simulation.stop();
        break;
    }
  }

  @Override
  public void step(int count) throws Exception {
    for (int cnt = 1; cnt <= count; cnt++) {
      simulation.step();
    }
  }

  @Override
  public boolean isRunning() throws Exception {
    return simulation.isRunning();
  }

  @Override
  public SimulationState loadState(String key) throws Exception {
    File simulationFile = new File(getSimulationFilename(key));
    byte[] data = new byte[(int) simulationFile.length()];
    InputStream in = new GZIPInputStream(new FileInputStream(simulationFile));
    data = IOUtils.readBytesFromStream(in);
    in.close();
    AntSimulation.SimulationState stateProto = AntSimulation.SimulationState.parseFrom(data);
    return ProtoConversionUtil.convert(stateProto);
  }

  @Override
  public void saveState(String key, SimulationState state) throws Exception {
    OutputStream out = new GZIPOutputStream(new FileOutputStream(getSimulationFilename(key)));
    try {
      AntSimulation.SimulationState stateProto = ProtoConversionUtil.convert(state);
      out.write(stateProto.toByteArray());
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
    return simulation.getDeviceInfo();
  }

  /**
   * @return the file name for the simulation with the given key
   */
  private String getSimulationFilename(String key) {
    return "simulation/" + key;
  }
}
