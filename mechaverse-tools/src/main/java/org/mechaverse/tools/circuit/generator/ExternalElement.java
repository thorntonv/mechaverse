package org.mechaverse.tools.circuit.generator;

import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.circuit.model.Output;

/**
 * An element that is external to the logical unit.
 *
 * @author thorntonv@mechaverse.org
 */
public class ExternalElement extends Element {

  public static final String TYPE = "external";

  public static class ExternalElementType extends ElementType {

    public static final ExternalElementType INSTANCE = new ExternalElementType();

    private static final long serialVersionUID = 1L;

    private final Output output = new Output();

    public ExternalElementType() {
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
  private final String elementId;
  private final String outputId;

  public ExternalElement(
      int relativeUnitRow, int relativeUnitColumn, String elementId, String outputId) {
    this.relativeUnitRow = relativeUnitRow;
    this.relativeUnitColumn = relativeUnitColumn;
    this.elementId = elementId;
    this.outputId = outputId;

    setType(TYPE);
  }

  public int getRelativeUnitRow() {
    return relativeUnitRow;
  }

  public int getRelativeUnitColumn() {
    return relativeUnitColumn;
  }

  public String getElementId() {
    return elementId;
  }

  public String getOutputId() {
    return outputId;
  }
}
