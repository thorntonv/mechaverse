#include <gtest/gtest.h>

#include <core/simulation/ant/SimulationImpl.h>

using namespace mechaverse;

TEST(SimulationImpl, getState) {
  SimulationImpl simulation;
  EXPECT_TRUE(simulation.getStateByteSize() > 0);
}
