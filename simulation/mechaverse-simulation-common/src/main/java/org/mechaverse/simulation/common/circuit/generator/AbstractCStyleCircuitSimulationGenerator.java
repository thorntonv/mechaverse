package org.mechaverse.simulation.common.circuit.generator;

import java.io.PrintWriter;

import org.mechaverse.circuit.model.Output;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ExternalElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;

/**
 * An implementation of {@link CircuitSimulationGenerator} for C style languages.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class AbstractCStyleCircuitSimulationGenerator
    extends AbstractCircuitSimulationGenerator {

  private static final boolean PRINT_DEBUG_INFO = false;

  protected final String luIndexExpr;
  protected final int numExternalElements;
  protected final int numLogicalUnits;

  public AbstractCStyleCircuitSimulationGenerator(CircuitSimulationModel model) {
    super(model);

    this.luIndexExpr = getLogicalUnitIndexExpr();
    this.numLogicalUnits = model.getLogicalUnitCount();
    this.numExternalElements = model.getLogicalUnitInfo().getExternalElements().size();
  }

  protected abstract String getLogicalUnitIndexExpr();

  protected abstract void printExternalElementDebugInfo(
      String outputVarName, String stateIndexExpr, PrintWriter out);

  protected abstract void printUpdateDebugInfo(ElementInfo element, Output output,
      String updateExpr, LogicalUnitInfo logicalUnitInfo, PrintWriter out);

  /**
   * Generates code to copy state values to external input variables.
   *
   * @param stateArrayVarName the name of the array that contains state values
   */
  protected void generateCopyStateValuesToExternalInputs(
      String stateArrayVarName, LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    for (ExternalElementInfo externalElementRef : logicalUnitInfo.getExternalElements()) {
      ElementInfo externalElementInfo = logicalUnitInfo.getElementInfo(
          externalElementRef.getElement().getElementId());
      String externalElementVarName =
          externalElementInfo.getOutputVarName(externalElementRef.getElement().getOutputId());
      int externalLogicalUnitRelativeIdx =
          getRelativeLogicalUnitIndex(externalElementRef.getElement());
      int externalStateIdx = logicalUnitInfo.getStateIndex(externalElementVarName);

      // An expression that evaluates to the index of the external logical unit. This is computed
      // by adding the external relative logical unit index to the logical unit index and taking the
      // modulus to ensure that the index is in a valid range.
      String externalLogicalUnitIndexExpr = String.format("(%s + %d + %d) %% %d",
          luIndexExpr, externalLogicalUnitRelativeIdx, numLogicalUnits, numLogicalUnits);

      for (String outputVarName : externalElementRef.getOutputVarNames()) {
        String stateIndexExpr = getStateIndexExpr(externalLogicalUnitIndexExpr, externalStateIdx);
        if (PRINT_DEBUG_INFO) {
          printExternalElementDebugInfo(outputVarName, stateIndexExpr, out);
        }
        out.printf("int %s = %s[%s];%n", outputVarName, stateArrayVarName, stateIndexExpr);
      }
    }
  }

  /**
   * Generates code to copy external input values to a state array.
   *
   * @param stateArrayVarName the name of the array that contains state values
   */
  protected void generateCopyExternalInputsToState(
      String stateArrayVarName, LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    String[] varNames = model.getLogicalUnitInfo().getVarNames();
    for (int idx = 0; idx < logicalUnitInfo.getExternalElements().size(); idx++) {
      out.printf("%s[(%d * %d) + %s] = %s;%n",
          stateArrayVarName, idx, numLogicalUnits, luIndexExpr, varNames[idx]);
    }
  }

  /**
   * Generates code to copy state values from an array into local variables.
   */
  protected void generateCopyStateValuesToVariables(
      LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    String[] varNames = logicalUnitInfo.getVarNames();
    for(int idx = 0; idx < varNames.length; idx++) {
      out.println(loadStateToVarStatement(varNames[idx], idx));
    }
  }

  /**
   * Generates code to set constants.
   */
  protected void generateConstants(LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Output output : element.getOutputs()) {
        if (isConstantExpression(element, output)) {
          String updateExpr = getVarMappedExpression(element, output);
          if (PRINT_DEBUG_INFO) {
            printUpdateDebugInfo(element, output, updateExpr, logicalUnitInfo, out);
          }
          out.printf("%s = %s;%n", element.getOutputVarName(output), updateExpr);
        }
      }
    }
  }

  /**
   * Generates code to update state variables.
   */
  protected void generateUpdates(LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Output output : element.getOutputs()) {
        if (!isConstantExpression(element, output)) {
          String updateExpr = getVarMappedExpression(element, output);
          if (PRINT_DEBUG_INFO) {
            printUpdateDebugInfo(element, output, updateExpr, logicalUnitInfo, out);
          }
          out.printf("%s = %s;%n", element.getOutputVarName(output), updateExpr);
        }
      }
    }
  }

  /**
   * Generates code to copy local variables to the state array.
   */
  protected void generateCopyVariablesToState(LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Output output : element.getOutputs()) {
        String varName = element.getOutputVarName(output);
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
    return String.format("int %s = circuitState[%s];", varName,
        getStateIndexExpr(getLogicalUnitIndexExpr(), stateIndex));
  }

  private String saveVarToStateStatement(String varName, int stateIndex) {
    return String.format("circuitState[%s] = %s;",
        getStateIndexExpr(getLogicalUnitIndexExpr(), stateIndex), varName);
  }
}
