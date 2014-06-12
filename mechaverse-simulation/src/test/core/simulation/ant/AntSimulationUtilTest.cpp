#include <gtest/gtest.h>

#include <core/simulation/ant/AntSimulationUtil.h>
#include <core/simulation/ant/ant-simulation.pb.h>

using namespace mechaverse;

TEST(AntSimulationUtil, directionCW) {
  EXPECT_EQ(SOUTH_EAST, AntSimulationUtil::directionCW(EAST));
  EXPECT_EQ(SOUTH, AntSimulationUtil::directionCW(SOUTH_EAST));
  EXPECT_EQ(SOUTH_WEST, AntSimulationUtil::directionCW(SOUTH));
  EXPECT_EQ(WEST, AntSimulationUtil::directionCW(SOUTH_WEST));
  EXPECT_EQ(NORTH_WEST, AntSimulationUtil::directionCW(WEST));
  EXPECT_EQ(NORTH, AntSimulationUtil::directionCW(NORTH_WEST));
  EXPECT_EQ(NORTH_EAST, AntSimulationUtil::directionCW(NORTH));
  EXPECT_EQ(EAST, AntSimulationUtil::directionCW(NORTH_EAST));
}

TEST(AntSimulationUtil, directionCCW) {
  EXPECT_EQ(NORTH_EAST, AntSimulationUtil::directionCCW(EAST));
  EXPECT_EQ(NORTH, AntSimulationUtil::directionCCW(NORTH_EAST));
  EXPECT_EQ(NORTH_WEST, AntSimulationUtil::directionCCW(NORTH));
  EXPECT_EQ(WEST, AntSimulationUtil::directionCCW(NORTH_WEST));
  EXPECT_EQ(SOUTH_WEST, AntSimulationUtil::directionCCW(WEST));
  EXPECT_EQ(SOUTH, AntSimulationUtil::directionCCW(SOUTH_WEST));
  EXPECT_EQ(SOUTH_EAST, AntSimulationUtil::directionCCW(SOUTH));
  EXPECT_EQ(EAST, AntSimulationUtil::directionCCW(SOUTH_EAST));
}

TEST(AntSimulationUtil, canMoveAntToCell_emptyCell) {
  Cell cell = Cell(1, 1);
  EXPECT_TRUE(AntSimulationUtil::canMoveAntToCell(cell));
}

TEST(AntSimulationUtil, canMoveAntToCell_rockCell) {
  Cell cell = Cell(1, 1);
  Rock rock = Rock(Rock::default_instance());
  cell.setRock(&rock);
  EXPECT_FALSE(AntSimulationUtil::canMoveAntToCell(cell));
}
