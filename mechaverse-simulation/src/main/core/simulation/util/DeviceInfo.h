/*
 * DeviceInfo.h
 *
 *  Created on: Mar 27, 2014
 *      Author: thorntonv
 */

#ifndef DEVICEINFO_H_
#define DEVICEINFO_H_

#include <string>

namespace mechaverse {

class DeviceInfo {

 public:

  /**
   * Returns a string containing device information.
   */
  static std::string getDeviceInfo();

};

}

#endif /* DEVICEINFO_H_ */
