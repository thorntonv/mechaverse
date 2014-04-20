#include <core/simulation/Simulation.h>

#include <core/simulation/ant/SimulationImpl.h>

using namespace mechaverse;

/*
 * A simulation implementation which Forwards all methods to the ant simulation implementation.
 */

Simulation::Simulation() {
  this->simulationImpl = new SimulationImpl();
}

Simulation::~Simulation() {
  delete simulationImpl;
}

int Simulation::getStateByteSize() {
  return simulationImpl->getStateByteSize();
}

int Simulation::getState(char* data, int len) const {
  return simulationImpl->getState(data, len);
}

void Simulation::setState(char* data, int len) {
  simulationImpl->setState(data, len);
}

void Simulation::start() {
  simulationImpl->start();
}

void Simulation::stop() {
  simulationImpl->stop();
}

void Simulation::step() {
  simulationImpl->step();
}

bool Simulation::isRunning() {
  return simulationImpl->isRunning();
}

void Simulation::transferEntityIn(char* data, int len) {
  return simulationImpl->transferEntityIn(data, len);
}

int Simulation::getEntityOutByteSize() {
  return simulationImpl->getEntityOutByteSize();
}

int Simulation::transferEntityOut(char* data, int len) const {
  return simulationImpl->transferEntityOut(data, len);
}

std::string Simulation::getDeviceInfo() {
  return simulationImpl->getDeviceInfo();
}
