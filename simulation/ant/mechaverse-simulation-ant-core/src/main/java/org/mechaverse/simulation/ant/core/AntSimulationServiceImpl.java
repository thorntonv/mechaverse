package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.core.AntSimulationImpl;
import org.mechaverse.simulation.api.SimulationService;
import org.mechaverse.simulation.common.opencl.DeviceUtil;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link SimulationService}.
 */
@Service
public class AntSimulationServiceImpl implements SimulationService {

  private final AntSimulationImpl[] instances = {new AntSimulationImpl()};

  @Override
  public int getInstanceCount() throws Exception {
    return instances.length;
  }

  @Override
  public byte[] getState(int instanceIdx) throws Exception {
    return getInstance(instanceIdx).getState().serialize();
  }

  @Override
  public void setState(int instanceIdx, byte[] stateData) throws Exception {
    getInstance(instanceIdx).setState(AntSimulationState.deserialize(stateData));
  }

  @Override
  public byte[] getStateValue(int instanceIdx, String key) throws Exception {
    return getInstance(instanceIdx).getState().getData(key);
  }

  @Override
  public void setStateValue(int instanceIdx, String key, byte[] value) throws Exception {
    getInstance(instanceIdx).getState().setData(key, value);
  }

  @Override
  public byte[] generateRandomState() throws Exception {
    return AntSimulationImpl.randomState().serialize();
  }

  @Override
  public void step(int instanceIdx, int stepCount) throws Exception {
    for (int cnt = 1; cnt <= stepCount; cnt++) {
      getInstance(instanceIdx).step();
    }
  }

  @Override
  public String getDeviceInfo(int instanceIdx) {
    return DeviceUtil.getDeviceInfo();
  }

  private AntSimulationImpl getInstance(int idx) {
    return instances[idx];
  }
}
