package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mechaverse.cellautomaton.model.*;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.ExternalCell.ExternalCellType;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterables;

/**
 * A model used for generating a cellular automaton simulation.
 */
public class CellularAutomatonSimulationModel {

  public static final String EXTERNAL_INPUT_ID_PREFIX = "in";

  /**
   * An input that is connected to the output of a cell.
   */
  public static class Input {

    private Cell cell;
    private Output output;

    public Input(Cell cell, Output output) {
      this.cell = cell;
      this.output = output;
    }

    public Cell getCell() {
      return cell;
    }

    public Output getOutput() {
      return output;
    }
  }

  public static class ExternalCellInfo extends CellInfo {

    private final ExternalCell cell;

    public ExternalCellInfo(ExternalCell cell, Map<String, String> outputVarNameMap) {
      super(cell, ExternalCellType.INSTANCE, new Input[] {}, outputVarNameMap,
        Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(),
        Collections.<String, Map<String, String>>emptyMap());
      this.cell = cell;
    }

    @Override
    public ExternalCellType getType() {
      return ExternalCellType.INSTANCE;
    }

    @Override
    public ExternalCell getCell() {
      return cell;
    }
  }

  /**
   * Information about an {@link Cell}.
   */
  public static class CellInfo {

    private final Cell cell;
    private final CellType cellType;
    private final Input[] inputs;
    private final Map<String, String> outputVarNameMap;
    private final Map<String, String> paramVarNameMap;
    private final Map<String, String> varNameMap;
    // Maps an output id to a param id map.
    private final Map<String, Map<String, String>> outputParamIdMap;

    public CellInfo(Cell cell, CellType cellType, Input[] inputs,
        Map<String, String> outputVarNameMap, Map<String, String> paramVarNameMap,
        Map<String, String> varNameMap, Map<String, Map<String, String>> outputParamIdMap) {
      this.cell = cell;
      this.cellType = cellType;
      this.inputs = inputs;
      this.outputVarNameMap = outputVarNameMap;
      this.paramVarNameMap = paramVarNameMap;
      this.varNameMap = varNameMap;
      this.outputParamIdMap = outputParamIdMap;
    }

    public String getId() {
      return cell.getId();
    }

    public Cell getCell() {
      return cell;
    }

    public CellType getType() {
      return cellType;
    }

    public List<Output> getOutputs() {
      return cellType.getOutputs();
    }

    public Input[] getInputs() {
      return inputs;
    }

    public String getOutputVarName(Output output) {
      return getOutputVarName(output.getId());
    }

    public String getOutputVarName(String outputId) {
      return outputVarNameMap.get(outputId);
    }

    public Collection<String> getOutputVarNames() {
      return outputVarNameMap.values();
    }

    public String getParamVarName(Param param) {
      return getParamVarName(param.getId());
    }

    public String getParamVarName(String paramId) {
      return paramVarNameMap.get(paramId);
    }

    public Collection<String> getParamVarNames() {
      return paramVarNameMap.values();
    }

    public String getVarName(Var var) {
      return getVarName(var.getId());
    }

    public String getVarName(String paramId) {
      return varNameMap.get(paramId);
    }

    public Collection<String> getVarNames() {
      return varNameMap.values();
    }

    public String getOutputParamVarName(Output output, Param param) {
      return getOutputParamVarName(output, param.getId());
    }

    public String getOutputParamVarName(Output output, String paramId) {
      Map<String, String> paramIdMap = outputParamIdMap.get(output.getId());
      if (paramIdMap != null) {
        return getParamVarName(paramIdMap.get(paramId));
      }
      return null;
    }

    public Collection<String> getOutputParamVarNames(Output output) {
      Map<String, String> paramIdMap = outputParamIdMap.get(output.getId());
      if (paramIdMap != null) {
        return paramIdMap.keySet();
      }
      return null;
    }

    public int getStateSize() {
      return outputVarNameMap.size() + paramVarNameMap.size();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      CellInfo other = (CellInfo) obj;
      if (cell == null) {
        if (other.cell != null) {
          return false;
        }
      } else if (!cell.getId().equals(other.cell.getId())) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      return cell.getId().hashCode();
    }
  }

  /**
   * Cell connection information.
   *
   * @author Vance Thornton (thorntonv@mechaverse.org)
   */
  public static class ConnectionInfo {

    private Map<Cell, Input[]> cellInputMap;
    private List<ExternalCell> externalCells;

    public ConnectionInfo(Map<Cell, Input[]> cellInputMap,
        List<ExternalCell> externalCells) {
      this.cellInputMap = cellInputMap;
      this.externalCells = externalCells;
    }

    public Iterable<Cell> getCells() {
      return cellInputMap.keySet();
    }

    public Input[] getInputs(Cell cell) {
      return cellInputMap.get(cell);
    }

    public List<ExternalCell> getExternalCells() {
      return externalCells;
    }
  }

  /**
   * Information about a {@link LogicalUnit}.
   */
  public static class LogicalUnitInfo {

    private final int width;
    private final int height;
    private final List<CellInfo> cells;
    private final List<ExternalCellInfo> externalCells;
    private final ImmutableBiMap<String, Integer> varNameStateIndexMap;

    public LogicalUnitInfo(int width, int height, List<CellInfo> cells,
        List<ExternalCellInfo> externalCells, ImmutableBiMap<String, Integer> varNameStateIndexMap) {
      this.width = width;
      this.height = height;
      this.cells = cells;
      this.externalCells = externalCells;
      this.varNameStateIndexMap = varNameStateIndexMap;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }

    public List<CellInfo> getCells() {
      return cells;
    }

    public List<ExternalCellInfo> getExternalCells() {
      return externalCells;
    }

    /**
     * @return the cell with the given id
     */
    public CellInfo getCellInfo(String id) {
      for (CellInfo cell : Iterables.concat(getCells(), getExternalCells())) {
        if (cell.getId().equals(id)) {
          return cell;
        }
      }
      return null;
    }

    public CellInfo getCellInfo(int row, int col) {
      return cells.get(row * width + col);
    }

    /**
     * @return the external cell with the given id.
     */
    public ExternalCellInfo getExternalCellInfo(String id) {
      for (ExternalCellInfo cellInfo : getExternalCells()) {
        if (cellInfo.getId().equals(id)) {
          return cellInfo;
        }
      }
      return null;
    }

    /**
     * @return the index at which the value of the given variable is stored in the state
     */
    public int getStateIndex(String varName) {
      return varNameStateIndexMap.get(varName);
    }

    /**
     * Returns the variable names ordered by state index.
     */
    public String[] getVarNames() {
      String[] varNames = new String[getStateSize()];
      for (int idx = 0; idx < varNames.length; idx++) {
        varNames[idx] = varNameStateIndexMap.inverse().get(idx);
      }
      return varNames;
    }

    /**
     * @return the number of state values
     */
    public int getStateSize() {
      return varNameStateIndexMap.size();
    }
  }

  protected final CellularAutomatonDescriptor descriptor;
  protected final Map<String, CellType> cellTypeMap;
  protected final LogicalUnitInfo logicalUnitInfo;

  public CellularAutomatonSimulationModel(CellularAutomatonDescriptor descriptor,
      Map<String, CellType> cellTypeMap, LogicalUnitInfo logicalUnitInfo) {
    this.descriptor = descriptor;
    this.logicalUnitInfo = logicalUnitInfo;
    this.cellTypeMap = cellTypeMap;
  }

  public CellularAutomatonDescriptor getDescriptor() {
    return descriptor;
  }

  /**
   * @return the type of the cell with the given id
   */
  public CellType getCellType(String typeId) {
    return cellTypeMap.get(typeId);
  }

  public LogicalUnitInfo getLogicalUnitInfo() {
    return logicalUnitInfo;
  }

  public int getWidth() {
    return descriptor.getWidth();
  }

  public int getHeight() {
    return descriptor.getHeight();
  }

  public int getIterationsPerUpdate() {
    return descriptor.getIterationsPerUpdate() != null ? descriptor.getIterationsPerUpdate() : 1;
  }

  public String getValueType() {
    return descriptor.getValueType() != null ? descriptor.getValueType() : "int";
  }

  /**
   * Returns the number of state values.
   */
  public int getStateSize() {
    return logicalUnitInfo.getStateSize() * getLogicalUnitCount();
  }

  /**
   * Returns the number of cell output state values.
   */
  public int getCellOutputStateSize() {
    int outputStateSize = 0;
    for (CellInfo cellInfo : getLogicalUnitInfo().getCells()) {
      outputStateSize += cellInfo.getOutputVarNames().size();
    }
    outputStateSize *= getLogicalUnitCount();
    return outputStateSize;
  }

  /**
   * Returns the size of the cellular automaton state in bytes.
   */
  public int getStateSizeBytes() {
    return getStateSize() * 4;
  }

  public int getLogicalUnitCount() {
    return descriptor.getWidth() * descriptor.getHeight();
  }

  public CellInfo getCell(int row, int col) {
    row = row % logicalUnitInfo.getHeight();
    col = col % logicalUnitInfo.getWidth();
    return logicalUnitInfo.getCellInfo(row, col);
  }
}
