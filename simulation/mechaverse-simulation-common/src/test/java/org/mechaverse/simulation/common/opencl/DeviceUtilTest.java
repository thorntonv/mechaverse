package org.mechaverse.simulation.common.opencl;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

public class DeviceUtilTest {

  private Logger logger = Logger.getLogger(DeviceUtilTest.class);

  @Test
  public void getDeviceInfo() {
    String deviceInfo = DeviceUtil.getDeviceInfo();
    logger.debug("Device info: " + deviceInfo);
    assertTrue(deviceInfo.length() > 0);
  }
}
