package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.api.SimulationService;
import org.mechaverse.simulation.common.opencl.DeviceUtil;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link SimulationService}.
 */
@Service
public class AntSimulationServiceImpl implements SimulationService {

  private AntSimulationPool instancePool = new FixedAntSimulationPool(1);

  @Override
  public int getInstanceCount() throws Exception {
    return instancePool.size();
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
    return AntSimulation.randomState().serialize();
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

  private AntSimulation getInstance(int idx) {
    return instancePool.getInstance(idx);
  }
}
