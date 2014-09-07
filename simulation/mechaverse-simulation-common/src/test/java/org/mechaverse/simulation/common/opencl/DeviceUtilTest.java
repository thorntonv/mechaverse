package org.mechaverse.simulation.common.opencl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for {@link DeviceUtil}.
 */
public class DeviceUtilTest {

  private Logger logger =  LoggerFactory.getLogger(DeviceUtilTest.class);

  @Test
  public void getDeviceInfo() {
    String deviceInfo = DeviceUtil.getDeviceInfo();
    logger.debug("Device info: " + deviceInfo);
    assertTrue(deviceInfo.length() > 0);
  }
}
