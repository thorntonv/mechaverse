#ifndef ANTSIMULATIONUTIL_H_
#define ANTSIMULATIONUTIL_H_

#include <core/simulation/ant/ant-simulation.pb.h>
#include <core/simulation/ant/Cell.h>

namespace mechaverse {

/**
 * Utility methods for the ant simulation.
 */
class AntSimulationUtil {

 public:

  /**
   * @return the next direction clockwise from the given direction
   */
  static Direction directionCW(const Direction& direction);

  /**
   * @return the next direction counter clockwise from the given direction
   */
  static Direction directionCCW(const Direction& direction);

  /**
   * @return true if an ant can move to the given cell, false otherwise
   */
  static bool canMoveAntToCell(const Cell& cell);

 private:

  AntSimulationUtil();
  virtual ~AntSimulationUtil();

};

}

#endif /* ANTSIMULATIONUTIL_H_ */
