/*
 * Simulation.h
 *
 *  Created on: Dec 25, 2013
 *      Author: thorntonv
 */

#ifndef SIMULATION_H_
#define SIMULATION_H_

#include <string>

namespace mechaverse {

/**
 * An interface for the mechaverse simulation.
 */
class Simulation {

 public:

  Simulation();
  virtual ~Simulation();

  /**
   * @return the size of the serialized current state in bytes
   */
  int getStateByteSize();

  /**
   * Gets the current state.
   *
   * @param data a buffer to hold the serialized data
   * @param len the length of the buffer
   * @return the size of the serialized state data in bytes
   */
  int getState(char* data, int len) const;

  /**
   * Sets the current state.
   *
   * @param data a buffer which contains the serialized state data
   * @param len the size of the serialized state data
   */
  void setState(char* data, int len);

  /**
   * Starts the simulation. The simulation will continue to execute asynchronously until stopped.
   */
  void start();

  /**
   * Stops the simulation. Has no effect if the simulation is not running.
   */
  void stop();

  /**
   * Execute a single step of the simulation.
   */
  void step();

  /**
   * @return true if the simulation is running, false otherwise
   */
  bool isRunning();

  /**
   * Attempts to transfer an entity into the simulation.
   *
   * @param data a buffer containing serialized entity data
   * @param len the size of the data in bytes
   */
  void transferEntityIn(char* data, int len);

  /**
   * @return the size of the serialized entity to transfer out of the simulation or 0 if no entity
   * transfer is pending
   */
  int getEntityOutByteSize();

  /**
   * Attempts to transfer an entity out of the simulation.
   * @param data a buffer to hold the serialized entity data
   * @param len the size of the buffer in bytes
   * @return the size of the serialized entity data
   */
  int transferEntityOut(char* data, int len) const;

  /**
   * @return a string containing device information
   */
  std::string getDeviceInfo();

 private:

  class SimulationImpl* simulationImpl;

};

}

#endif /* SIMULATION_H_ */
