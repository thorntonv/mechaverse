#include <core/simulation/ant/SimulationImpl.h>
#include <core/simulation/ant/AntSimulationUtil.h>

#include <core/simulation/util/DeviceInfo.h>

using namespace mechaverse;

/**
 * Initializes the simulation to the default state.
 */
SimulationImpl::SimulationImpl()
: state(SimulationState::default_instance()), cells() {
  state.set_id("ab1fd5f0-7b4f-11e3-981f-0800200c9a66");
  Environment* env = state.mutable_environment();
  env->set_id("748fa2e0-750a-11e3-981f-0800200c9a66");
  env->set_width(10);
  env->set_height(10);
  cells.setEnvironment(env);
}

SimulationImpl::~SimulationImpl() {
}

int SimulationImpl::getStateByteSize() {
  return state.ByteSize();
}

int SimulationImpl::getState(char* data, int len) const {
  state.SerializeToArray(data, len);
  return len;
}

void SimulationImpl::setState(char* data, int len) {
  state.ParseFromArray(data, len);
  cells.setEnvironment(state.mutable_environment());
}

void SimulationImpl::start() {
}

void SimulationImpl::stop() {
}

void SimulationImpl::step() {
  Environment* env = state.mutable_environment();
  Environment_EnvironmentStats* stats = env->mutable_stats();
  stats->set_age(stats->age() + 1);

  for (int idx = 0; idx < env->ant_size(); idx++) {
    updateAnt(env->mutable_ant(idx));
  }
}

bool SimulationImpl::isRunning() {
  return false;
}

void SimulationImpl::transferEntityIn(char* data, size_t len) {
  // TODO(thorntonv): Implement this method.
}

int SimulationImpl::getEntityOutByteSize() {
  // TODO(thorntonv): Implement this method.
  return 0;
}

int SimulationImpl::transferEntityOut(char* data, size_t len) const {
  // TODO(thorntonv): Implement this method.
  return 0;
}

std::string SimulationImpl::getDeviceInfo() {
  return DeviceInfo::getDeviceInfo();
}

void SimulationImpl::turnEntityCCW(EntityState* entityState) {
  entityState->set_direction(AntSimulationUtil::directionCCW(entityState->direction()));
}

void SimulationImpl::updateAnt(Ant* ant) {
  // TODO(thorntonv): Replace this hard coded behavior.
  if (!moveAntForward(ant)) {
    turnEntityCCW(ant->mutable_entity_state());
  }
}

bool SimulationImpl::moveAntForward(Ant* ant) {
  const EntityState& entityState = ant->entity_state();
  Cell& cell = cells.getCell(entityState);

  if (cells.hasCellInDirection(cell, entityState.direction())) {
    Cell& targetCell = cells.cellInDirection(cell, entityState.direction());
    if (AntSimulationUtil::canMoveAntToCell(targetCell)) {
      moveAntToCell(ant, targetCell);
      return true;
    }
  }
  return false;
}

void SimulationImpl::moveAntToCell(Ant* ant, Cell& cell) {
  EntityState* entityState = ant->mutable_entity_state();
  cells.getCell(entityState).setAnt(NULL);
  entityState->set_x(cell.getCol());
  entityState->set_y(cell.getRow());
  cell.setAnt(ant);
}
