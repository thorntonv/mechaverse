#ifndef CELL_H_
#define CELL_H_

#include <vector>

#include <core/simulation/ant/ant-simulation.pb.h>

namespace mechaverse {

/**
 * A single cell in the environment.
 */
class Cell {

 private:

  int row;
  int col;
  Ant* ant;
  Barrier* barrier;
  Conduit* conduit;
  Dirt* dirt;
  Food* food;
  Pheromone* pheromone;
  Rock* rock;

 public:

  Cell(int row = -1, int col = -1);
  virtual ~Cell();

  inline int getRow() const {
    return row;
  }

  inline int getCol() const {
    return col;
  }

  inline Ant* getAnt() const {
    return ant;
  }

  inline void setAnt(Ant* ant) {
    this->ant = ant;
  }

  inline Barrier* getBarrier() const {
    return barrier;
  }

  inline void setBarrier(Barrier* barrier) {
    this->barrier = barrier;
  }

  inline Conduit* getConduit() const {
    return conduit;
  }

  inline void setConduit(Conduit* conduit) {
    this->conduit = conduit;
  }

  inline Dirt* getDirt() const {
    return dirt;
  }

  inline void setDirt(Dirt* dirt) {
    this->dirt = dirt;
  }

  inline Food* getFood() const {
    return food;
  }

  inline void setFood(Food* food) {
    this->food = food;
  }

  inline Pheromone* getPheromone() const {
    return pheromone;
  }

  inline void setPheromone(Pheromone* pheromone) {
    this->pheromone = pheromone;
  }

  inline Rock* getRock() const {
    return rock;
  }

  inline void setRock(Rock* rock) {
    this->rock = rock;
  }
};

}

#endif /* CELL_H_ */
