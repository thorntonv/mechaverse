package org.mechaverse.simulation.common.circuit;

import static org.junit.Assert.assertEquals;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.circuit.model.LogicalUnit;
import org.mechaverse.circuit.model.Output;
import org.mechaverse.circuit.model.Param;
import org.mechaverse.circuit.model.Row;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ExternalElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.Input;
import org.mechaverse.simulation.common.circuit.generator.ExternalElement;

public class CircuitTestUtil {

  public static final String ROUTING_3IN3OUT_TYPE = "routing3in3out";

  public static class ElementInfoVerifier {

    private ElementInfo elementInfo;

    public ElementInfoVerifier(ElementInfo elementInfo) {
      this.elementInfo = elementInfo;
    }

    public ElementInfoVerifier verifyId(String id) {
      assertEquals(id, elementInfo.getId());
      return this;
    }

    public ElementInfoVerifier verifyType(String type) {
      assertEquals(type, elementInfo.getElement().getType());
      return this;
    }

    public ElementInfoVerifier verifyInputCount(int count) {
      assertEquals(count, elementInfo.getInputs().length);
      return this;
    }

    public ElementInfoVerifier verifyInput(int inputIdx, ElementInfo expectedElement,
        Output expectedOutput) {
      Input input = elementInfo.getInputs()[inputIdx];
      assertEquals(expectedElement.getElement(), input.getElement());
      assertEquals(expectedOutput, input.getOutput());
      return this;
    }

    public ElementInfoVerifier verifyExternalInput(int inputIdx, String expectedId) {
      Input input = elementInfo.getInputs()[inputIdx];
      assertEquals(ExternalElement.TYPE, input.getElement().getType());
      assertEquals(expectedId, input.getElement().getId());
      return this;
    }

    public ElementInfoVerifier verifyInputIds(String... ids) {
      assertEquals(ids.length, elementInfo.getInputs().length);
      for (int idx = 0; idx < ids.length; idx++) {
        assertEquals(ids[idx], elementInfo.getInputs()[idx].getElement().getId());
      }
      return this;
    }

    public ElementInfoVerifier verifyOutputVarNames(String... varNames) {
      assertEquals(varNames.length, elementInfo.getOutputs().size());
      assertEquals(varNames.length, elementInfo.getOutputVarNames().size());
      for (int idx = 0; idx < varNames.length; idx++) {
        assertEquals(varNames[idx], elementInfo.getOutputVarName(
          elementInfo.getOutputs().get(idx)));
      }
      return this;
    }

    /**
     * Verifies output parameter variable names. Matrix has the following structure:
     * <pre>
     * {
     * // Output 1
     * { {"paramId", "paramVarName"}, {"paramId", "paramVarName"}, ... }
     * // Output 2
     * { {"paramId", "paramVarName"}, {"paramId", "paramVarName"}, ... }
     * ...
     * }
     * </pre>
     */
    public ElementInfoVerifier verifyOutputParamVarNames(String[][][] matrix) {
      for (int outputIdx = 0; outputIdx < matrix.length; outputIdx++) {
        verifyOutputParamVarNames(outputIdx, matrix[outputIdx]);
      }
      return this;
    }

    public ElementInfoVerifier verifyOutputParamVarNames(int outputIdx, String[][] matrix) {
      for (int idx = 0; idx < matrix.length; idx++) {
        assertEquals(matrix[idx][1], elementInfo.getOutputParamVarName(
            elementInfo.getOutputs().get(outputIdx), matrix[idx][0]));
      }
      return this;
    }

    public static ElementInfoVerifier of(ElementInfo elementInfo) {
      return new ElementInfoVerifier(elementInfo);
    }
  }

  public static class ExternalElementInfoVerifier {

    private ExternalElement element;

    public ExternalElementInfoVerifier(ExternalElementInfo elementInfo) {
      this.element = elementInfo.getElement();
    }

    public ExternalElementInfoVerifier verifyRelativeRow(int relativeRow) {
      assertEquals(relativeRow, element.getRelativeUnitRow());
      return this;
    }

    public ExternalElementInfoVerifier verifyRelativeColumn(int relativeColumn) {
      assertEquals(relativeColumn, element.getRelativeUnitColumn());
      return this;
    }

    public ExternalElementInfoVerifier verifyElementId(String id) {
      assertEquals(id, element.getElementId());
      return this;
    }

    public ExternalElementInfoVerifier verifyOutputId(String id) {
      assertEquals(id, element.getOutputId());
      return this;
    }

    public static ExternalElementInfoVerifier of(ExternalElementInfo elementInfo) {
      return new ExternalElementInfoVerifier(elementInfo);
    }
  }

  /**
   * Creates a test circuit with a logical unit that consists of a matrix of elements of the given
   * type.
   *
   * @param width the number of logical units per row
   * @param height the number of logical unit rows
   * @param elementType the elementType
   * @param unitRowCount the number of rows in a logical unit
   * @param unitColumnCount the number of columns in a logical unit
   */
  public static Circuit createTestCircuit(int width, int height, ElementType elementType,
      int unitRowCount, int unitColumnCount) {
    Circuit circuit = new Circuit();
    circuit.setWidth(width);
    circuit.setHeight(height);
    circuit.getElementTypes().add(elementType);
    circuit.setLogicalUnit(new LogicalUnit());
    for (int rowCount = 1; rowCount <= unitRowCount; rowCount++) {
      Row row = new Row();
      for (int colCount = 1; colCount <= unitColumnCount; colCount++) {
        row.getElements().add(createElement(elementType.getId()));
      }
      circuit.getLogicalUnit().getRows().add(row);
    }
    return circuit;
  }

  public static ElementType createRouting3in3OutElementType() {
    ElementType routing = new ElementType();
    routing.setId(ROUTING_3IN3OUT_TYPE);
    routing.getOutputs().add(createOutput(
      "1", "({input2} & {input2Mask}) | ({input3} & {input3Mask})", "input2Mask", "input3Mask"));
    routing.getOutputs().add(createOutput(
      "2", "({input1} & {input1Mask}) | ({input3} & {input3Mask})", "input1Mask", "input3Mask"));
    routing.getOutputs().add(createOutput(
      "3", "({input1} & {input1Mask}) | ({input2} & {input2Mask})", "input1Mask", "input2Mask"));
    return routing;
  }

  public static Output createOutput(String id, String expression, String... paramIds) {
    Output output = new Output();
    output.setId(id);
    output.setExpression(expression);
    for (String paramId : paramIds) {
      Param param = new Param();
      param.setId(paramId);
      output.getParams().add(param);
    }
    return output;
  }

  public static Element createElement(String type) {
    Element element = new Element();
    element.setType(type);
    return element;
  }
}
