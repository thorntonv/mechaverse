package org.mechaverse.gwt.common.client.util;

/**
 * A coordinate which consists of a row and column.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class Coordinate {

  private int row;
  private int column;

  public Coordinate(int row, int column) {
    super();
    this.row = row;
    this.column = column;
  }

  public int getRow() {
    return row;
  }

  public int getColumn() {
    return column;
  }

  public static Coordinate create(int row, int column) {
    return new Coordinate(row, column);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + column;
    result = prime * result + row;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Coordinate other = (Coordinate) obj;
    return column == other.column && row == other.row;
  }
}
