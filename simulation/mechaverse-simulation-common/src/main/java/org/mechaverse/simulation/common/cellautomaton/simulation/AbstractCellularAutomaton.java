package org.mechaverse.simulation.common.cellautomaton.simulation;

import org.apache.commons.math3.util.Pair;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;

import java.util.function.Supplier;

import java.util.IdentityHashMap;

/**
 * A base class for {@link CellularAutomaton} implementations that are not auto generated based on a
 * {@link CellularAutomatonDescriptor}.
 *  
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class AbstractCellularAutomaton implements CellularAutomaton {

  public static abstract class AbstractCell implements CellularAutomaton.Cell {

    protected int[] outputs;
    protected int[] nextOutputs;
    protected Cell[] neighbors;

    public abstract void beforeUpdate();

    public abstract void update();
    
    @Override
    public int getOutput(int idx) {
      return outputs[idx];
    }

    @Override
    public void setOutput(int idx, int value) {
      outputs[idx] = value;
    }

    @Override
    public int getOutputCount() {
      return outputs.length;
    }

    public Cell[] getNeighbors() {
      return this.neighbors;
    }
    
    public void setNeighbors(Cell[] neighbors) {
      this.neighbors = neighbors;
    }
  }
  
  private final AbstractCell[][] cells;
  private final IdentityHashMap<Cell, Pair<Integer, Integer>> cellPositionMap;

  protected AbstractCellularAutomaton(int width, int height,
      Supplier<? extends AbstractCell> cellSupplier, CellConnector cellConnector) {
    cells = new AbstractCell[height][width];
    this.cellPositionMap = new IdentityHashMap<>();

    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        cells[row][col] = cellSupplier.get();
        cellPositionMap.put(cells[row][col], new Pair<>(row, col));
      }
    }

    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        getCell(row, col).setNeighbors(cellConnector.getNeighbors(row, col, this));        
      }
    }
  }

  @Override
  public int getWidth() {
    return cells[0].length;
  }

  @Override
  public int getHeight() {
    return cells.length;
  }

  @Override
  public AbstractCell getCell(int row, int column) {
    row = (row + getHeight()) % getHeight();
    column = (column + getWidth()) % getWidth();
    return cells[row][column];
  }

  public Pair<Integer, Integer> getCellPosition(Cell cell) {
    return cellPositionMap.get(cell);
  }

  @Override
  public void update() {
    for (int row = 0; row < cells.length; row++) {
      for (int col = 0; col < cells[row].length; col++) {
        getCell(row, col).beforeUpdate();
      }
    }

    for (int row = 0; row < cells.length; row++) {
      for (int col = 0; col < cells[row].length; col++) {
        getCell(row, col).update();
      }
    }
  }

  public void updateInputs() {}
}
