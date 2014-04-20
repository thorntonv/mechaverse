#include "Cell.h"

using namespace mechaverse;

Cell::Cell(int row, int col)
: ant(NULL), barrier(NULL), conduit(NULL), dirt(NULL), food(NULL), pheromone(NULL), rock(NULL)
{
  this->row = row;
  this->col = col;
}

Cell::~Cell() {}
