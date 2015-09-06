package org.mechaverse.simulation.common.cellautomaton.simulation;

import org.mechaverse.cellautomaton.model.Cell;
import org.mechaverse.cellautomaton.model.LogicalUnit;
import org.mechaverse.cellautomaton.model.Row;

/**
 * Builds a {@link LogicalUnit}.
 */
public class LogicalUnitBuilder {

  private int width = 1;
  private int height = 1;
  private int neighborConnections = 4;
  private String defaultCellType;

  public LogicalUnitBuilder setWidth(int width) {
    this.width = width;
    return this;
  }

  public LogicalUnitBuilder setHeight(int height) {
    this.height = height;
    return this;
  }

  public LogicalUnitBuilder setNeighborConnections(int neighborConnections) {
    this.neighborConnections = neighborConnections;
    return this;
  }

  public LogicalUnitBuilder setDefaultCellType(final String defaultCellType) {
    this.defaultCellType = defaultCellType;
    return this;
  }

  public LogicalUnit build() {
    LogicalUnit logicalUnit = new LogicalUnit();
    logicalUnit.setNeighborConnections(String.valueOf(neighborConnections));

    for (int rowIdx = 0; rowIdx < height; rowIdx++) {
      Row row = new Row();

      for (int colIdx = 0; colIdx < width; colIdx++) {
        Cell cell = new Cell();
        cell.setType(defaultCellType);
        row.getCells().add(cell);
      }

      logicalUnit.getRows().add(row);
    }
    return logicalUnit;
  }
}
