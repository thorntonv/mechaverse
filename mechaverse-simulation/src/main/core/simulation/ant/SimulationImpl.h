#ifndef ANTSIMULATION_H_
#define ANTSIMULATION_H_

#include <core/simulation/ant/ant-simulation.pb.h>
#include <core/simulation/ant/CellEnvironment.h>

namespace mechaverse {

/**
 * Ant simulation implementation.
 *
 * @author thorntonv@mechaverse.org
 */
class SimulationImpl {

 private:

  SimulationState state;
  CellEnvironment cells;

 public:

  SimulationImpl();
  virtual ~SimulationImpl();

  int getStateByteSize();
  int getState(char* data, int len) const;

  void setState(char* data, int len);

  void start();
  void stop();
  void step();
  bool isRunning();

  void transferEntityIn(char* data, size_t len);

  int getEntityOutByteSize();
  int transferEntityOut(char* data, size_t len) const;

  std::string getDeviceInfo();

 private:

  /**
   * Turns the give entity to the next direction counter clockwise of its current direction.
   */
  void turnEntityCCW(EntityState* entityState);

  /**
   * Updates an ant.
   */
  void updateAnt(Ant* ant);

  /**
   * Moves the given ant forward one cell if permitted.
   */
  bool moveAntForward(Ant* ant);

  /**
   * Moves the given ant to the given cell.
   */
  void moveAntToCell(Ant* ant, Cell& cell);
};

}

#endif /* ANTSIMULATION_H_ */
