package org.mechaverse.simulation.common.opencl;

import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLPlatform;

/**
 * Utility methods for working with OpenCL devices.
 *
 * @author thorntonv@mechaverse.org
 */
public final class DeviceUtil {

  /**
   * Returns a string containing device information.
   */
  public static final String getDeviceInfo() {
    StringBuilder out = new StringBuilder();

    for (CLPlatform platform : CLPlatform.listCLPlatforms()) {
      for (CLDevice device : platform.listCLDevices()) {
        out.append(String.format("%s - %s%n", device.getName(), device.getVendor()));
      }
    }
    return out.toString();
  }
}
