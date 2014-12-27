package org.mechaverse.simulation.common.cellautomaton.simulation;

import java.util.Collection;

import org.mechaverse.cellautomaton.model.Cell;
import org.mechaverse.cellautomaton.model.CellType;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.cellautomaton.model.LogicalUnit;
import org.mechaverse.cellautomaton.model.Output;
import org.mechaverse.cellautomaton.model.Param;
import org.mechaverse.cellautomaton.model.Row;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;

import com.google.common.collect.ImmutableList;

/**
 * Assists with building {@link CellularAutomaton} instances.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CellularAutomatonBuilder {

  // TODO(thorntonv): Implement unit tests for this class.

  public static final String INPUT_TYPE = "input";

  /**
   * An input cell.
   */
  public static class InputCellType extends CellType {

    private static final long serialVersionUID = 1L;

    public InputCellType() {
      setId(INPUT_TYPE);
      getOutputs().add(newOutput("1", "automatonInput[{idx} % automatonInputLength] ", "idx"));
    }

    public static InputCellType newInstance() {
      return new InputCellType();
    }
  }

  public static final String ROUTING_3IN3OUT_TYPE = "routing3in3out";

  /**
   * A routing cell with 3 inputs and 3 outputs.
   */
  public static class Routing3In3OutCellType extends CellType {

    private static final long serialVersionUID = 1L;

    public Routing3In3OutCellType() {
      setId(ROUTING_3IN3OUT_TYPE);
      getOutputs().add(newOutput(
        "1", "({input2} & {input2Mask}) | ({input3} & {input3Mask})", "input2Mask", "input3Mask"));
      getOutputs().add(newOutput(
        "2", "({input1} & {input1Mask}) | ({input3} & {input3Mask})", "input1Mask", "input3Mask"));
      getOutputs().add(newOutput(
        "3", "({input1} & {input1Mask}) | ({input2} & {input2Mask})", "input1Mask", "input2Mask"));
    }

    public static Routing3In3OutCellType newInstance() {
      return new Routing3In3OutCellType();
    }
  }

  public static final String TOGGLE_TYPE = "toggle";

  /**
   * An cell that toggles between 1 and 0.
   */
  public static class ToggleCellType extends CellType {

    private static final long serialVersionUID = 1L;

    public ToggleCellType() {
      setId(TOGGLE_TYPE);
      getOutputs().add(newOutput("1", "~{output1}"));
      getOutputs().add(newOutput("2", "~{output2}"));
      getOutputs().add(newOutput("3", "~{output3}"));
    }

    public static ToggleCellType newInstance() {
      return new ToggleCellType();
    }
  }

  /**
   * Sets and gets cellular automaton state values.
   */
  public static class CellularAutomatonStateBuilder {

    protected int[] state;
    protected CellularAutomatonSimulationModel model;

    public static CellularAutomatonStateBuilder of(
        CellularAutomatonDescriptor descriptor, int count) {
      CellularAutomatonSimulationModel model =
          new CellularAutomatonSimulationModelBuilder().buildModel(descriptor);
      return new CellularAutomatonStateBuilder(new int[count * model.getStateSize()], model);
    }

    public CellularAutomatonStateBuilder(int[] state, CellularAutomatonSimulationModel model) {
      this.state = state;
      this.model = model;
    }

    public LogicalUnitStateBuilder luStateBuilder(int automatonIdx, int logicalUnitIdx) {
      return new LogicalUnitStateBuilder(state, logicalUnitIdx,
          automatonIdx * model.getStateSize(), model);
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
  public static class LogicalUnitStateBuilder extends CellularAutomatonStateBuilder {

    private int logicalUnitIndex;
    private int offset;

    public LogicalUnitStateBuilder(
        int[] state, int logicalUnitIndex, int offset, CellularAutomatonSimulationModel model) {
      super(state, model);
      this.logicalUnitIndex = logicalUnitIndex;
      this.offset = offset;
    }

    public int get(String varName) {
      return state[getStateIndex(varName)];
    }

    /**
     * Returns the state index of the given variable name for this logical unit.
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
   * Creates a cellular automaton descriptor with a logical unit that consists of a matrix of cells
   * of the given type.
   *
   * @param width the number of logical units per row
   * @param height the number of logical unit rows
   * @param cellType the cell type
   * @param unitRowCount the number of rows in a logical unit
   * @param unitColumnCount the number of columns in a logical unit
   */
  public static CellularAutomatonDescriptor newCellularAutomaton(int width, int height, 
      CellType cellType, int unitRowCount, int unitColumnCount) {
    String[][] cellTypeIds = new String[unitRowCount][unitColumnCount];
    for (int row = 0; row < cellTypeIds.length; row++) {
      for (int col = 0; col < cellTypeIds[row].length; col++) {
        cellTypeIds[row][col] = cellType.getId();
      }
    }
    return newCellularAutomaton(width, height, ImmutableList.of(cellType), cellTypeIds);
  }

  public static CellularAutomatonDescriptor newCellularAutomaton(int width, int height, 
      Collection<CellType> cellTypes, String[][] cellTypeIds) {
    CellularAutomatonDescriptor descriptor = new CellularAutomatonDescriptor();
    descriptor.setWidth(width);
    descriptor.setHeight(height);
    descriptor.getCellTypes().addAll(cellTypes);
    descriptor.setLogicalUnit(new LogicalUnit());
    for (int rowIdx = 0; rowIdx < cellTypeIds.length; rowIdx++) {
      Row row = new Row();
      for (int colIdx = 0; colIdx < cellTypeIds[rowIdx].length; colIdx++) {
        row.getCells().add(newCell(cellTypeIds[rowIdx][colIdx]));
      }
      descriptor.getLogicalUnit().getRows().add(row);
    }
    return descriptor;
  }

  /**
   * Creates a new output.
   */
  public static Output newOutput(String id, String expression, String... paramIds) {
    Output output = new Output();
    output.setId(id);
    output.setUpdateExpression(expression);
    for (String paramId : paramIds) {
      Param param = new Param();
      param.setId(paramId);
      output.getParams().add(param);
    }
    return output;
  }

  /**
   * Creates a new cell.
   */
  public static Cell newCell(String type) {
    Cell cell = new Cell();
    cell.setType(type);
    return cell;
  }
}
