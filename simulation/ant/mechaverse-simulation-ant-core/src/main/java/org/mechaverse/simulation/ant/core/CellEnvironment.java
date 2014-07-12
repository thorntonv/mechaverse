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
  private final Environment env;

  private final List<Ant> ants = new ArrayList<Ant>();

  public CellEnvironment(Environment env) {
    this.rowCount = env.getHeight();
    this.colCount = env.getWidth();
    this.cells = new Cell[env.getHeight()][env.getWidth()];
    this.env = env;

    // Allocate cells.
    for (int row = 0; row < env.getHeight(); row++) {
      for (int col = 0; col < env.getWidth(); col++) {
        cells[row][col] = new Cell(row, col);
      }
    }

    // Add entities to the appropriate cells.
    for (Entity entity : env.getEntities()) {
      setEntityCell(entity, getCell(entity));
    }
  }

  public Environment getEnvironment() {
    return env;
  }

  public int getRowCount() {
    return rowCount;
  }

  public int getColumnCount() {
    return colCount;
  }

  public List<Ant> getAnts() {
    return ants;
  }

  public boolean hasCell(int row, int col) {
    return row >= 0 && col >= 0 && row < cells.length && col < cells[row].length;
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

  public void addEntity(Entity entity, Cell cell) {
    env.getEntities().add(entity);
    setEntityCell(entity, cell);
  }

  public void moveAntToCell(Ant ant, Cell fromCell, Cell targetCell) {
    fromCell.setAnt(null);
    ant.setX(targetCell.getColumn());
    ant.setY(targetCell.getRow());
    targetCell.setAnt(ant);
  }

  private void setEntityCell(Entity entity, Cell cell) {
    entity.setX(cell.getColumn());
    entity.setY(cell.getRow());

    if (entity instanceof Ant) {
      cell.setAnt((Ant) entity);
      ants.add((Ant) entity);
    } else if (entity instanceof Barrier) {
      cell.setBarrier((Barrier) entity);
    } else if (entity instanceof Conduit) {
      cell.setConduit((Conduit) entity);
    } else if (entity instanceof Dirt) {
      cell.setDirt((Dirt) entity);
    } else if (entity instanceof Food) {
      cell.setFood((Food) entity);
    } else if (entity instanceof Pheromone) {
      cell.setPheromone((Pheromone) entity);
    } else if (entity instanceof Rock) {
      cell.setRock((Rock) entity);
    }
  }

  private boolean isValidCellCoordinate(int row, int column) {
    return row >= 0 && row < rowCount && column >= 0 && column < colCount;
  }
}
