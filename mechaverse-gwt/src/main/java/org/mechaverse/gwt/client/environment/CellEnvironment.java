package org.mechaverse.gwt.client.environment;

import java.util.Collections;
import java.util.List;

import org.mechaverse.api.model.simulation.ant.Entity;
import org.mechaverse.api.model.simulation.ant.Environment;

import com.google.common.collect.Lists;

/**
 * An environment which tracks entities on a per cell basis.
 *  
 * @author thorntonv@mechaverse.org
 */
public class CellEnvironment {

  /**
   * An individual cell. 
   */
  public class Cell {

    private final int row;
    private final int column;
    private final List<Entity> entities = Lists.newArrayList();

    public Cell(int row, int column) {
      this.row = row;
      this.column = column;
    }

    public int getRow() {
      return row;
    }

    public int getColumn() {
      return column;
    }

    public void add(Entity entity) {
      entity.setX(column);
      entity.setY(row);

      entities.add(entity);
      environment.getEntities().add(entity);
    }

    public void remove(Entity entity) {
      entities.remove(entity);
      environment.getEntities().remove(entity);
    }

    public void clear() {
      environment.getEntities().removeAll(entities);
      entities.clear();
    }
    
    public List<Entity> getEntities() {
      return Collections.unmodifiableList(entities);
    }
  }

  private Environment environment;
  private Cell cells[][];

  public CellEnvironment(Environment environment) {
    this.cells = new Cell[environment.getHeight()][environment.getWidth()];
    this.environment = environment;

    createCells();
  }

  private void createCells() {
    for (int row = 0; row < cells.length; row++) {
      for (int column = 0; column < cells[row].length; column++) {
        cells[row][column] = new Cell(row, column);
      }
    }

    for (Entity entity : environment.getEntities()) {
      getCell(entity.getY(), entity.getX()).entities.add(entity);
    }
  }

  public Cell getCell(int row, int column) {
    return cells[row][column];
  }

  public Environment getEnvironment() {
    return environment;
  }
}
