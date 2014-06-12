#include "DeviceInfo.h"

#include <iostream>
#include <fstream>
#include <sstream>

#define CL_USE_DEPRECATED_OPENCL_1_1_APIS
#include <CL/cl.h>
#undef CL_VERSION_1_2
#include <CL/cl.hpp>

using namespace std;
using namespace cl;
using namespace mechaverse;

std::string DeviceInfo::getDeviceInfo() {
  std::stringstream out;

  vector<Platform> platforms;
  Platform::get(&platforms);
  vector<Platform>::iterator platform;
  for (platform = platforms.begin(); platform != platforms.end(); ++platform) {
    vector<Device> devices;
    platform->getDevices(CL_DEVICE_TYPE_ALL, &devices);
    for (vector<Device>::iterator device = devices.begin(); device != devices.end(); ++device) {
      string deviceName, deviceVendor;
      if (device->getInfo(CL_DEVICE_NAME, &deviceName) == CL_SUCCESS
          && device->getInfo(CL_DEVICE_VENDOR, &deviceVendor) == CL_SUCCESS) {
        out << deviceName << " - " << deviceVendor << endl;
      }
    }
  }

  return out.str();
}
