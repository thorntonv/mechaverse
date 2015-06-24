package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mechaverse.cellautomaton.model.Cell;
import org.mechaverse.cellautomaton.model.CellType;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.cellautomaton.model.LogicalUnit;
import org.mechaverse.cellautomaton.model.Output;
import org.mechaverse.cellautomaton.model.Param;
import org.mechaverse.cellautomaton.model.Row;
import org.mechaverse.cellautomaton.model.Var;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.CellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.ConnectionInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.ExternalCellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.Input;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.LogicalUnitInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.ExternalCell.ExternalCellType;
import org.springframework.beans.BeanUtils;

import com.google.common.collect.ImmutableBiMap;

/**
 * Builds a {@link CellularAutomatonSimulationModel} for a {@link CellularAutomatonDescriptor}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CellularAutomatonSimulationModelBuilder {

  private static final Pattern ID_PATTERN = Pattern.compile("\\d+");

  protected abstract class AbstractCellInfoBuilder<C extends Cell, I extends CellInfo> {

    protected final C cell;
    protected final CellType cellType;
    protected final Input[] inputs;

    public AbstractCellInfoBuilder(C cell, CellType cellType, Input[] inputs) {
      this.cell = cell;
      this.cellType = cellType;
      this.inputs = inputs;
    }

    public I build() {
      Map<String, String> outputVarNameMap = new HashMap<>();
      Map<String, String> paramVarNameMap = new HashMap<>();
      Map<String, String> varNameMap = new HashMap<>();
      Map<String, Map<String, String>> outputParamIdMap = new HashMap<>();

      // Build a map that maps a param id (used to refer to the parameter in output expressions) to
      // its variable name.
      for (Param param : cellType.getParams()) {
        String varName = getCellParamVarName(cell, param);
        if (!paramVarNameMap.containsKey(param.getId())) {
          paramVarNameMap.put(param.getId(), varName);
        } else {
          throw new IllegalStateException("Parameter " + param.getId()
              + " is defined more than once for cell " + cell.getId());
        }
      }

      // Build a map that maps a variable id to its name.
      for (Var var : cellType.getVars()) {
        String varName = getCellVarName(cell, var);
        if (!varNameMap.containsKey(var.getId())) {
          varNameMap.put(var.getId(), varName);
        } else {
          throw new IllegalStateException("Variable " + var.getId()
              + " is defined more than once for cell " + cell.getId());
        }
      }

      for (Output output : cellType.getOutputs()) {
        outputVarNameMap.put(output.getId(), getCellOutputVarName(cell, output));
        Map<String, String> paramIdMap = new HashMap<>();
        for (Param param : output.getParams()) {
          String id = getCellOutputParamId(output, param.getId());
          String varName = getCellOutputParamVarName(cell, output, param);
          if (!paramVarNameMap.containsKey(id)) {
            paramIdMap.put(param.getId(), id);
            paramVarNameMap.put(id, varName);
          } else {
            throw new IllegalStateException("Parameter " + id
                + " is defined more than once for cell " + cell.getId());
          }
        }
        outputParamIdMap.put(output.getId(), paramIdMap);
      }
      return createCellInfo(outputVarNameMap, paramVarNameMap, varNameMap, outputParamIdMap);
    }

    protected abstract I createCellInfo(Map<String, String> outputVarNameMap,
        Map<String, String> paramVarNameMap, Map<String, String> varNameMap,
            Map<String, Map<String, String>> outputParamIdMap);
  }

  protected class CellInfoBuilder extends AbstractCellInfoBuilder<Cell, CellInfo> {

    public CellInfoBuilder(Cell cell, CellType cellType, Input[] inputs) {
      super(cell, cellType, inputs);
    }

    @Override
    protected CellInfo createCellInfo(Map<String, String> outputVarNameMap,
        Map<String, String> paramVarNameMap, Map<String, String> varNameMap,
            Map<String, Map<String, String>> outputParamIdMap) {
      return new CellInfo(cell, cellType, inputs, outputVarNameMap, paramVarNameMap, varNameMap,
          outputParamIdMap);
    }
  }

  protected class ExternalCellInfoBuilder
      extends AbstractCellInfoBuilder<ExternalCell, ExternalCellInfo> {

    public ExternalCellInfoBuilder(ExternalCell cell) {
      super(cell, ExternalCellType.INSTANCE, new Input[] {});
    }

    @Override
    protected ExternalCellInfo createCellInfo(Map<String, String> outputVarNameMap,
        Map<String, String> paramVarNameMap, Map<String, String> varNameMap,
            Map<String, Map<String, String>> outputParamIdMap) {
      return new ExternalCellInfo(cell, outputVarNameMap);
    }
  }

  /**
   * Builds the simulation model for the given cellular automaton.
   */
  public static CellularAutomatonSimulationModel build(CellularAutomatonDescriptor descriptor) {
    return new CellularAutomatonSimulationModelBuilder().buildModel(descriptor);
  }

  public CellularAutomatonSimulationModel buildModel(CellularAutomatonDescriptor descriptor) {
    Map<String, CellType> cellTypeMap = buildCellTypeMap(descriptor);
    LogicalUnitInfo logicalUnitInfo =
        buildLogicalUnitInfo(descriptor.getLogicalUnit(), cellTypeMap);
    return new CellularAutomatonSimulationModel(descriptor, cellTypeMap, logicalUnitInfo);
  }

  private Map<String, CellType> buildCellTypeMap(CellularAutomatonDescriptor descriptor) {
    Map<String, CellType> map = new HashMap<>();
    map.put(ExternalCell.TYPE, ExternalCellType.INSTANCE);
    for (CellType cellType : descriptor.getCellTypes()) {
      map.put(cellType.getId(), preprocessCellType(cellType));
    }
    return map;
  }

  /**
   * Preprocesses a cell type. If an output has an id that specified multiple outputs eg. {1,2,3}
   * then the output will be duplicated for each of the specified ids.
   */
  private CellType preprocessCellType(CellType cellType) {
    CellType processedCellType = new CellType();
    processedCellType.setId(cellType.getId());
    processedCellType.getParams().addAll(cellType.getParams());
    processedCellType.getVars().addAll(cellType.getVars());

    for (Output output : cellType.getOutputs()) {
      Matcher matcher = ID_PATTERN.matcher(output.getId());
      while (matcher.find()) {
        Output newOutput = new Output();
        newOutput.setId(matcher.group());
        newOutput.getParams().addAll(output.getParams());
        newOutput.setBeforeUpdate(output.getBeforeUpdate());
        newOutput.setUpdateExpression(output.getUpdateExpression());
        newOutput.setConstant(output.isConstant());
        processedCellType.getOutputs().add(newOutput);
      }
    }
    return processedCellType;
  }

  private LogicalUnitInfo buildLogicalUnitInfo(
      LogicalUnit unit, Map<String, CellType> cellTypeMap) {
    // Build cell matrix.
    Cell[][] matrix = new Cell[unit.getRows().size()][];
    int id = 1;
    for (int rowIdx = 0; rowIdx < matrix.length; rowIdx++) {
      Row row = unit.getRows().get(rowIdx);
      matrix[rowIdx] = new Cell[row.getCells().size()];
      for (int colIdx = 0; colIdx < matrix[rowIdx].length; colIdx++) {
        // Create a copy of the cell because it will be modified as part of building the model.
        Cell cell = new Cell();
        BeanUtils.copyProperties(row.getCells().get(colIdx), cell);

        if (cell.getId() == null) {
          // If the cell does not have an id assign one.
          cell.setId(String.valueOf(id));
          id++;
        }
        matrix[rowIdx][colIdx] = preprocessCell(cell, cellTypeMap);
      }
    }

    // Connect cells.
    ConnectionInfoBuilder connectionInfoBuilder =
        new EightNeighborConnectionInfoBuilder(matrix, cellTypeMap);
    if (unit.getNeighborConnections() != null) {
      if (unit.getNeighborConnections().equals("3")) {
        connectionInfoBuilder = new ThreeNeighborConnectionInfoBuilder(matrix, cellTypeMap);
      } else if (unit.getNeighborConnections().equals("4")) {
        connectionInfoBuilder = new FourNeighborConnectionInfoBuilder(matrix, cellTypeMap);
      }
    }
    ConnectionInfo connectionInfo = connectionInfoBuilder.build();

    List<CellInfo> cells = new ArrayList<>();
    List<ExternalCellInfo> externalCells = new ArrayList<>();
    for (Cell cell : connectionInfo.getCells()) {
      CellType cellType = cellTypeMap.get(cell.getType());
      Input[] inputs = connectionInfo.getInputs(cell);
      cells.add(new CellInfoBuilder(cell, cellType, inputs).build());
    }
    for (ExternalCell externalCell : connectionInfo.getExternalCells()) {
      externalCells.add(new ExternalCellInfoBuilder(externalCell).build());
    }
    return new LogicalUnitInfo(matrix[0].length, matrix.length, cells, externalCells,
        buildVarNameStateIndexMap(cells, externalCells));
  }

  /**
   * Preprocesses a cell. If the outputs attribute is set a new cell type will be created for the
   * cell that only includes the specified outputs.
   */
  private Cell preprocessCell(Cell cell, Map<String, CellType> cellTypeMap) {
    if (cell.getOutputs() != null) {
      CellType cellType = cellTypeMap.get(cell.getType());
      CellType newCellType = new CellType();
      newCellType.getParams().addAll(cellType.getParams());
      newCellType.setId(cell.getType() + "_e" + cell.getId());

      Matcher matcher = ID_PATTERN.matcher(cell.getOutputs());
      while (matcher.find()) {
        String outputId = matcher.group();
        Output output = getOutput(outputId, cellType);
        if (output != null) {
          newCellType.getOutputs().add(output);
        }
      }
      cellTypeMap.put(newCellType.getId(), newCellType);
      cell.setType(newCellType.getId());
    }
    return cell;
  }

  private Output getOutput(String id, CellType cellType) {
    for (Output output : cellType.getOutputs()) {
      if (output.getId().equalsIgnoreCase(id)) {
        return output;
      }
    }
    return null;
  }

  protected ImmutableBiMap<String, Integer> buildVarNameStateIndexMap(
      Iterable<CellInfo> cells, Iterable<ExternalCellInfo> externalCells) {
    int stateIndex = 0;
    ImmutableBiMap.Builder<String, Integer> varNameStateIndexMapBuilder = ImmutableBiMap.builder();

    // Outputs that are the input of an external cell come first.
    for (CellInfo cell : cells) {
      for (Output output : cell.getOutputs()) {
        if (isExternalInput(cell, output, externalCells)) {
          String varName = cell.getOutputVarName(output);
          varNameStateIndexMapBuilder.put(varName, stateIndex++);
        }
      }
    }

    for (CellInfo cell : cells) {
      for (Output output : cell.getOutputs()) {
        if (!isExternalInput(cell, output, externalCells)) {
          String varName = cell.getOutputVarName(output);
          varNameStateIndexMapBuilder.put(varName, stateIndex++);
        }
      }
    }

    for (CellInfo cell : cells) {
      for (Param param : cell.getType().getParams()) {
        String varName = cell.getParamVarName(param);
        varNameStateIndexMapBuilder.put(varName, stateIndex++);
      }
      for (Output output : cell.getOutputs()) {
        for (Param param : output.getParams()) {
          String varName = cell.getOutputParamVarName(output, param);
          varNameStateIndexMapBuilder.put(varName, stateIndex++);
        }
      }
    }
    return varNameStateIndexMapBuilder.build();
  }

  private boolean isExternalInput(
      CellInfo cell, Output output, Iterable<ExternalCellInfo> externalCells) {
    for (ExternalCellInfo externalCell : externalCells) {
      if (externalCell.getCell().getCellId().equals(cell.getId())
          && externalCell.getCell().getOutputId().equals(output.getId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the name of the variable which stores the current value of the given input
   */
  protected String getVarName(Input input) {
    return getCellOutputVarName(input.getCell(), input.getOutput());
  }

  /**
   * @return the name of the variable that stores the current output value of a cell
   */
  protected String getCellOutputVarName(Cell cell, Output output) {
    if(cell.getType().equals(ExternalCell.TYPE)) {
      return String.format("ex_%s", cell.getId());
    }
    return String.format("cell_%s_out%s", cell.getId(), output.getId());
  }

  /**
   * @return the name of the variable that stores the value of a cell parameter
   */
  protected String getCellParamVarName(Cell cell, Param param) {
    return String.format("cell_%s_%s", cell.getId(), param.getId());
  }

  /**
   * Returns the name of a cell variable.
   */
  protected String getCellVarName(Cell cell, Var var) {
    return String.format("cell_%s_%s", cell.getId(), var.getId());
  }

  /**
   * @return the id of a cell output parameter that is used to refer to the parameter in output
   *         expressions
   */
  protected String getCellOutputParamId(Output output, String paramId) {
    return String.format("out%s_%s", output.getId(), paramId);
  }

  /**
   * @return the name of the variable that stores the value of a cell output parameter
   */
  protected String getCellOutputParamVarName(Cell cell, Output output, Param param) {
    return String.format("cell_%s_out%s_%s", cell.getId(), output.getId(), param.getId());
  }
}
