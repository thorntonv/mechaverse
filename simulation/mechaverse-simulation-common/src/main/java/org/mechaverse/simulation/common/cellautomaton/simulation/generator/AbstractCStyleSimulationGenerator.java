package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import org.mechaverse.cellautomaton.model.Output;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.CellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.ExternalCellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.LogicalUnitInfo;
import org.mechaverse.simulation.common.util.IndentPrintWriter;

/**
 * An implementation of {@link CellularAutomatonSimulationGenerator} for C style languages.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class AbstractCStyleSimulationGenerator
    extends AbstractCellularAutomatonSimulationGenerator {

  private static final boolean PRINT_DEBUG_INFO = false;

  protected final String luIndexExpr;
  protected final int numExternalCells;
  protected final int numLogicalUnits;

  public AbstractCStyleSimulationGenerator(CellularAutomatonSimulationModel model) {
    super(model);

    this.luIndexExpr = getLogicalUnitIndexExpr();
    this.numLogicalUnits = model.getLogicalUnitCount();
    this.numExternalCells = model.getLogicalUnitInfo().getExternalCells().size();
  }

  protected abstract String getLogicalUnitIndexExpr();

  protected abstract void printExternalCellDebugInfo(
      String outputVarName, String stateIndexExpr, IndentPrintWriter out);

  protected abstract void printUpdateDebugInfo(CellInfo cell, Output output,
      String updateExpr, LogicalUnitInfo logicalUnitInfo, IndentPrintWriter out);

  
  protected void generateExternalInputIndexVars(String stateArrayVarName,
      LogicalUnitInfo logicalUnitInfo, IndentPrintWriter out) {
    for (int idx = 0; idx < logicalUnitInfo.getExternalCells().size(); idx++) {
      ExternalCellInfo externalCellInfo = logicalUnitInfo.getExternalCells().get(idx);

      String externalLogicalUnitIndexExpr =
          String.format("((luRow + %d + %d) %% %d) * %d + (luCol + %d + %d) %% %d",
              externalCellInfo.getCell().getRelativeUnitRow(), model.getHeight(),
              model.getHeight(), model.getWidth(), externalCellInfo.getCell()
                  .getRelativeUnitColumn(), model.getWidth(), model.getWidth());

      for (String outputVarName : externalCellInfo.getOutputVarNames()) {
        String stateIndexExpr = getStateIndexExpr(externalLogicalUnitIndexExpr, idx);
        out.printf("int %s_idx = %s;%n", outputVarName, stateIndexExpr);
      }
    }
  }

  /**
   * Generates code to copy state values to external input variables.
   *
   * @param stateArrayVarName the name of the array that contains state values
   */
  protected void generateCopyStateValuesToExternalInputs(
      String stateArrayVarName, LogicalUnitInfo logicalUnitInfo, IndentPrintWriter out) {
    for (int idx = 0; idx < logicalUnitInfo.getExternalCells().size(); idx++) {
      ExternalCellInfo externalCellInfo = logicalUnitInfo.getExternalCells().get(idx);
      for (String outputVarName : externalCellInfo.getOutputVarNames()) {
        out.printf("int %s = %s[%s_idx];%n", outputVarName, stateArrayVarName, outputVarName);
      }
    }
  }

  /**
   * Generates code to copy external input values to a state array.
   *
   * @param stateArrayVarName the name of the array that contains state values
   */
  protected void generateCopyExternalInputsToState(
      String stateArrayVarName, LogicalUnitInfo logicalUnitInfo, IndentPrintWriter out) {

    for(int idx = 0; idx < logicalUnitInfo.getExternalCells().size(); idx++) {
      ExternalCell externalCell = logicalUnitInfo.getExternalCells().get(idx).getCell();
      CellInfo cellRef = logicalUnitInfo.getCellInfo(externalCell.getCellId());
      
      out.printf("%s[(%d * %d) + %s] = %s;%n", stateArrayVarName, idx, numLogicalUnits, luIndexExpr, 
          cellRef.getOutputVarName(externalCell.getOutputId()));          
    }
  }

  /**
   * Generates code to copy state values from an array into local variables.
   */
  protected void generateCopyStateValuesToVariables(
      LogicalUnitInfo logicalUnitInfo, IndentPrintWriter out) {
    String[] varNames = logicalUnitInfo.getVarNames();
    for(int idx = 0; idx < varNames.length; idx++) {
      out.println(loadStateToVarStatement(varNames[idx], idx));
    }
  }

  /**
   * Generates code to update constant values.
   */
  protected void generateConstants(LogicalUnitInfo logicalUnitInfo, IndentPrintWriter out) {
    // Generate statements executed before update.
    for (CellInfo cell : logicalUnitInfo.getCells()) {
      for (Output output : cell.getOutputs()) {
        if (output.isConstant() && output.getBeforeUpdate() != null) {
          String statements = getVarMappedString(output.getBeforeUpdate(), cell, output);
          out.printf("%s%n", statements);
        }
      }
    }

    // Generate update statements.
    for (CellInfo cell : logicalUnitInfo.getCells()) {
      for (Output output : cell.getOutputs()) {
        if (output.isConstant() && output.getUpdateExpression() != null) {
          String updateExpr = getVarMappedString(output.getUpdateExpression(), cell, output);
          out.printf("%s = %s;%n", cell.getOutputVarName(output), updateExpr);
        }
      }
    }
  }
  
  /**
   * Generates code to update state variables.
   */
  protected void generateUpdates(LogicalUnitInfo logicalUnitInfo, IndentPrintWriter out) {
    // Generate statements executed before update.
    for (CellInfo cell : logicalUnitInfo.getCells()) {
      for (Output output : cell.getOutputs()) {
        if (!output.isConstant() && output.getBeforeUpdate() != null) {
          String statements = getVarMappedString(output.getBeforeUpdate(), cell, output);
          if (PRINT_DEBUG_INFO) {
            printUpdateDebugInfo(cell, output, statements, logicalUnitInfo, out);
          }
          out.printLines(statements);
        }
      }
    }

    // Generate update statements.
    for (CellInfo cell : logicalUnitInfo.getCells()) {
      for (Output output : cell.getOutputs()) {
        if (!output.isConstant() && output.getUpdateExpression() != null) {
          String updateExpr = getVarMappedString(output.getUpdateExpression(), cell, output);
          if (PRINT_DEBUG_INFO) {
            printUpdateDebugInfo(cell, output, updateExpr, logicalUnitInfo, out);
          }
          out.printf("%s = %s;%n", cell.getOutputVarName(output), updateExpr);
        }
      }
    }
  }

  /**
   * Generates code to copy local variables to the state array.
   */
  protected void generateCopyVariablesToState(LogicalUnitInfo logicalUnitInfo, IndentPrintWriter out) {
    for (CellInfo cell : logicalUnitInfo.getCells()) {
      for (Output output : cell.getOutputs()) {
        String varName = cell.getOutputVarName(output);
        int stateIndex = logicalUnitInfo.getStateIndex(varName);
        out.println(saveVarToStateStatement(varName, stateIndex));
      }
    }
  }

  /**
   * Returns an expression that evaluates to the index into the state array for the given logical
   * unit and value indices.
   *
   * @param logicalUnitIndexExpr an expression that evaluates to a logical unit index
   * @param stateIndex the index of a state value relative to the logical unit.
   */
  private String getStateIndexExpr(String logicalUnitIndexExpr, int stateIndex) {
    return String.format("(%d * %d) + %s", stateIndex, numLogicalUnits, logicalUnitIndexExpr);
  }

  private String loadStateToVarStatement(String varName, int stateIndex) {
    return String.format("int %s = automatonState[%s];", varName,
        getStateIndexExpr(getLogicalUnitIndexExpr(), stateIndex));
  }

  private String saveVarToStateStatement(String varName, int stateIndex) {
    return String.format("automatonState[%s] = %s;",
        getStateIndexExpr(getLogicalUnitIndexExpr(), stateIndex), varName);
  }
}
