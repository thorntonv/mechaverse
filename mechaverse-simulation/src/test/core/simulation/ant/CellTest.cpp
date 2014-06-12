#include <gtest/gtest.h>

#include <core/simulation/ant/Cell.h>

using namespace mechaverse;

TEST(Cell, newCell) {
  Cell cell = Cell(5, 7);
  EXPECT_EQ(5, cell.getRow());
  EXPECT_EQ(7, cell.getCol());
}
