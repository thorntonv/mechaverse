package org.mechaverse.simulation.ant.core;

import java.util.ArrayList;
import java.util.List;

import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Barrier;
import org.mechaverse.simulation.ant.api.model.Conduit;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Dirt;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.Food;
import org.mechaverse.simulation.ant.api.model.Pheromone;
import org.mechaverse.simulation.ant.api.model.Rock;

public final class CellEnvironment {

  private final int rowCount;
  private final int colCount;
  private final Cell[][] cells;

  private final List<Ant> ants = new ArrayList<Ant>();

  public CellEnvironment(Environment env) {
    this.rowCount = env.getHeight();
    this.colCount = env.getWidth();
    this.cells = new Cell[env.getHeight()][env.getWidth()];

    // Allocate cells.
    for (int row = 0; row < env.getHeight(); row++) {
      for (int col = 0; col < env.getWidth(); col++) {
        cells[row][col] = new Cell(row, col);
      }
    }

    // Add entities to the appropriate cells.
    for (Entity entity : env.getEntities()) {
      if (entity instanceof Ant) {
        getCell(entity).setAnt((Ant) entity);
        ants.add((Ant) entity);
      } else if (entity instanceof Barrier) {
        getCell(entity).setBarrier((Barrier) entity);
      } else if (entity instanceof Conduit) {
        getCell(entity).setConduit((Conduit) entity);
      } else if (entity instanceof Dirt) {
        getCell(entity).setDirt((Dirt) entity);
      } else if (entity instanceof Food) {
        getCell(entity).setFood((Food) entity);
      } else if (entity instanceof Pheromone) {
        getCell(entity).setPheromone((Pheromone) entity);
      } else if (entity instanceof Rock) {
        getCell(entity).setRock((Rock) entity);
      }
    }
  }

  public List<Ant> getAnts() {
    return ants;
  }

  public Cell getCell(int row, int col) {
    return cells[row][col];
  }

  public Cell getCell(Entity entity) {
    return cells[entity.getY()][entity.getX()];
  }

  public Cell getCellInDirection(Cell cell, Direction direction) {
    int row = cell.getRow();
    int col = cell.getColumn();
    switch(direction) {
      case EAST:
        col++;
        break;
      case NORTH_EAST:
        row--;
        col++;
        break;
      case NORTH:
        row--;
        break;
      case NORTH_WEST:
        row--;
        col--;
        break;
      case WEST:
        col--;
        break;
      case SOUTH_WEST:
        row++;
        col--;
        break;
      case SOUTH:
        row++;
        break;
      case SOUTH_EAST:
        row++;
        col++;
        break;
    }
    return isValidCellCoordinate(row, col) ? cells[row][col] : null;
  }

  private boolean isValidCellCoordinate(int row, int column) {
    return row >= 0 && row < rowCount && column >= 0 && column < colCount;
  }
}
