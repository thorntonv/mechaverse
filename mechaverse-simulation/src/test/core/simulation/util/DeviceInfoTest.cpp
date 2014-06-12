#include <gtest/gtest.h>

#include <core/simulation/util/DeviceInfo.h>

using namespace mechaverse;

TEST(DeviceInfoTest, getDeviceInfo) {
  EXPECT_TRUE(DeviceInfo::getDeviceInfo().length() > 0);
}
