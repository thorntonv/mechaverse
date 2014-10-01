package org.mechaverse.simulation.common.circuit;

import java.util.Collection;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.circuit.model.LogicalUnit;
import org.mechaverse.circuit.model.Output;
import org.mechaverse.circuit.model.Param;
import org.mechaverse.circuit.model.Row;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;

import com.google.common.collect.ImmutableList;

/**
 * Assists with building {@link Circuit} instances.
 *
 * @author thorntonv@mechaverse.org
 */
public class CircuitBuilder {

  // TODO(thorntonv): Implement unit tests for this class.

  public static final String INPUT_TYPE = "input";

  /**
   * An input element.
   */
  public static class InputElementType extends ElementType {

    private static final long serialVersionUID = 1L;

    public InputElementType() {
      setId(INPUT_TYPE);
      getOutputs().add(newOutput("1", "circuitInput[{idx} % circuitInputLength] ", "idx"));
    }

    public static InputElementType newInstance() {
      return new InputElementType();
    }
  }

  public static final String ROUTING_3IN3OUT_TYPE = "routing3in3out";

  /**
   * A routing element with 3 inputs and 3 outputs.
   */
  public static class Routing3In3OutElementType extends ElementType {

    private static final long serialVersionUID = 1L;

    public Routing3In3OutElementType() {
      setId(ROUTING_3IN3OUT_TYPE);
      getOutputs().add(newOutput(
        "1", "({input2} & {input2Mask}) | ({input3} & {input3Mask})", "input2Mask", "input3Mask"));
      getOutputs().add(newOutput(
        "2", "({input1} & {input1Mask}) | ({input3} & {input3Mask})", "input1Mask", "input3Mask"));
      getOutputs().add(newOutput(
        "3", "({input1} & {input1Mask}) | ({input2} & {input2Mask})", "input1Mask", "input2Mask"));
    }

    public static Routing3In3OutElementType newInstance() {
      return new Routing3In3OutElementType();
    }
  }

  /**
   * Sets and gets circuit state values.
   */
  public static class CircuitStateBuilder {

    protected int[] state;
    protected CircuitSimulationModel model;

    public static CircuitStateBuilder of(Circuit circuit, int circuitCount) {
      CircuitSimulationModel model = new CircuitSimulationModelBuilder().buildModel(circuit);
      return new CircuitStateBuilder(new int[circuitCount * model.getCircuitStateSize()], model);
    }

    public CircuitStateBuilder(int[] state, CircuitSimulationModel model) {
      this.state = state;
      this.model = model;
    }

    public LogicalUnitStateBuilder luStateBuilder(int circuitIdx, int logicalUnitIdx) {
      return new LogicalUnitStateBuilder(state, logicalUnitIdx,
          circuitIdx * model.getCircuitStateSize(), model);
    }

    public int[] getState() {
      return state;
    }

    public void setAll(int value) {
      for (int idx = 0; idx < state.length; idx++) {
        state[idx] = value;
      }
    }
  }

  /**
   * Sets and gets logical unit state values.
   */
  public static class LogicalUnitStateBuilder extends CircuitStateBuilder {

    private int logicalUnitIndex;
    private int offset;

    public LogicalUnitStateBuilder(
        int[] state, int logicalUnitIndex, int offset, CircuitSimulationModel model) {
      super(state, model);
      this.logicalUnitIndex = logicalUnitIndex;
      this.offset = offset;
    }

    public int get(String varName) {
      return state[getStateIndex(varName)];
    }

    /**
     * Returns the circuit state index of the given variable name for this logical unit.
     */
    public int getStateIndex(String varName) {
      int stateIndex = model.getLogicalUnitInfo().getStateIndex(varName);
      return offset + logicalUnitIndex + stateIndex * model.getLogicalUnitCount();
    }

    public LogicalUnitStateBuilder set(String varName, int value) {
      int stateIndex = model.getLogicalUnitInfo().getStateIndex(varName);
      state[offset + logicalUnitIndex + stateIndex * model.getLogicalUnitCount()] = value;
      return this;
    }
  }

  /**
   * Creates a circuit with a logical unit that consists of a matrix of elements of the given type.
   *
   * @param width the number of logical units per row
   * @param height the number of logical unit rows
   * @param elementType the elementType
   * @param unitRowCount the number of rows in a logical unit
   * @param unitColumnCount the number of columns in a logical unit
   */
  public static Circuit newCircuit(int width, int height, ElementType elementType,
      int unitRowCount, int unitColumnCount) {
    String[][] elementTypeIds = new String[unitRowCount][unitColumnCount];
    for (int row = 0; row < elementTypeIds.length; row++) {
      for (int col = 0; col < elementTypeIds[row].length; col++) {
        elementTypeIds[row][col] = elementType.getId();
      }
    }
    return newCircuit(width, height, ImmutableList.of(elementType), elementTypeIds);
  }

  public static Circuit newCircuit(int width, int height, Collection<ElementType> elementTypes,
      String[][] elementTypeIds) {
    Circuit circuit = new Circuit();
    circuit.setWidth(width);
    circuit.setHeight(height);
    circuit.getElementTypes().addAll(elementTypes);
    circuit.setLogicalUnit(new LogicalUnit());
    for (int rowIdx = 0; rowIdx < elementTypeIds.length; rowIdx++) {
      Row row = new Row();
      for (int colIdx = 0; colIdx < elementTypeIds[rowIdx].length; colIdx++) {
        row.getElements().add(newElement(elementTypeIds[rowIdx][colIdx]));
      }
      circuit.getLogicalUnit().getRows().add(row);
    }
    return circuit;
  }

  /**
   * Creates a new output.
   */
  public static Output newOutput(String id, String expression, String... paramIds) {
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

  /**
   * Creates a new element.
   */
  public static Element newElement(String type) {
    Element element = new Element();
    element.setType(type);
    return element;
  }
}
