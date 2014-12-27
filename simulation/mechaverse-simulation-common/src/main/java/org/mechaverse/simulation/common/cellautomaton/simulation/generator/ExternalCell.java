package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import org.mechaverse.cellautomaton.model.Cell;
import org.mechaverse.cellautomaton.model.CellType;
import org.mechaverse.cellautomaton.model.Output;

/**
 * An cell that is external to the logical unit.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class ExternalCell extends Cell {

  public static final String TYPE = "external";

  public static class ExternalCellType extends CellType {

    public static final ExternalCellType INSTANCE = new ExternalCellType();

    private static final long serialVersionUID = 1L;

    private final Output output = new Output();

    public ExternalCellType() {
      setId(TYPE);
      output.setId("1");
      getOutputs().add(output);
    }

    public Output getOutput() {
      return output;
    }
  }

  private static final long serialVersionUID = 1L;

  private final int relativeUnitRow;
  private final int relativeUnitColumn;
  private final String cellId;
  private final String outputId;

  public ExternalCell(
      int relativeUnitRow, int relativeUnitColumn, String cellId, String outputId) {
    this.relativeUnitRow = relativeUnitRow;
    this.relativeUnitColumn = relativeUnitColumn;
    this.cellId = cellId;
    this.outputId = outputId;

    setType(TYPE);
  }

  public int getRelativeUnitRow() {
    return relativeUnitRow;
  }

  public int getRelativeUnitColumn() {
    return relativeUnitColumn;
  }

  public String getCellId() {
    return cellId;
  }

  public String getOutputId() {
    return outputId;
  }
}
