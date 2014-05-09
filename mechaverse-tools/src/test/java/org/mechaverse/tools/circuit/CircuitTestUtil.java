package org.mechaverse.tools.circuit;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.circuit.model.LogicalUnit;
import org.mechaverse.circuit.model.Output;
import org.mechaverse.circuit.model.Param;
import org.mechaverse.circuit.model.Row;

public class CircuitTestUtil {

  public static final String ROUTING_3IN3OUT_TYPE = "routing3in3out";

  /**
   * Creates a test circuit with two routing3in3out elements in the same row.
   */
  public static Circuit createTestCircuit1() {
    Circuit circuit = new Circuit();
    circuit.getElementTypes().add(createRouting3in3OutElementType());
    circuit.setLogicalUnit(new LogicalUnit());
    Row row = new Row();
    row.getElements().add(createElement(ROUTING_3IN3OUT_TYPE));
    row.getElements().add(createElement(ROUTING_3IN3OUT_TYPE));
    circuit.getLogicalUnit().getRows().add(row);
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
