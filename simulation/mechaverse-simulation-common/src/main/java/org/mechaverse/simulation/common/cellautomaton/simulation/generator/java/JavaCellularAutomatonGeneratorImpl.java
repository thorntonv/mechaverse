package org.mechaverse.simulation.common.cellautomaton.simulation.generator.java;

import java.io.PrintWriter;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.cellautomaton.model.Output;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.AbstractCStyleSimulationGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.CellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.Input;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.LogicalUnitInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.mechaverse.simulation.common.util.IndentPrintWriter;

/**
 * A generator that creates Java source code for executing a cellular automaton simulation.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class JavaCellularAutomatonGeneratorImpl extends AbstractCStyleSimulationGenerator {

  public static final String TYPE = "java";

  static final String IMPL_PACKAGE =
      "org.mechaverse.simulation.common.cellautomaton.simulation.generator.java";

  static final String IMPL_CLASS_NAME = "CellularAutomatonSimulationImpl";

  private final int inputSize;
  private final int outputSize;

  public JavaCellularAutomatonGeneratorImpl(
      CellularAutomatonDescriptor descriptor, int inputSize, int outputSize) {
    this(new CellularAutomatonSimulationModelBuilder().buildModel(descriptor), inputSize,
        outputSize);
  }

  public JavaCellularAutomatonGeneratorImpl(CellularAutomatonSimulationModel model, int inputSize,
      int outputSize) {
    super(model);
    if (!"int".equalsIgnoreCase(model.getValueType())) {
      throw new RuntimeException("Unsupported value type " + model.getValueType());
    }
    this.inputSize = inputSize;
    this.outputSize = outputSize;
  }

  @Override
  protected String getLogicalUnitIndexExpr() {
    return "luIndex";
  }

  @Override
  public void generate(final PrintWriter printWriter) {
    final IndentPrintWriter out = new IndentPrintWriter(printWriter);

    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();

    out.printf("package %s;%n", IMPL_PACKAGE);
    out.println("public class CellularAutomatonSimulationImpl extends "
        + "AbstractJavaCellularAutomatonSimulationImpl {").indent();
    generateConstructor(logicalUnitInfo, out);
    generateUpdateExternalInputs(logicalUnitInfo, out);
    generateLogicalUnitUpdateMethod(logicalUnitInfo, out);
    out.unindent().println("}");
    out.flush();
  }

  private void generateConstructor(LogicalUnitInfo logicalUnitInfo, IndentPrintWriter out) {
    out.println("public CellularAutomatonSimulationImpl() {").indent();
    out.printf("super(%d, %d, %d, %d, %d, %d);%n", model.getLogicalUnitCount(), numExternalCells,
        model.getStateSize(), inputSize, outputSize,
            model.getIterationsPerUpdate());
    out.unindent().println("}");
  }

  private void generateUpdateExternalInputs(LogicalUnitInfo logicalUnitInfo,
                                            IndentPrintWriter out) {
    out.println("@Override");
    out.printf("public void updateExternalInputs(int %s) {%n", luIndexExpr).indent();
    
    generateCopyStateValuesToVariables(logicalUnitInfo, out);
    generateCopyExternalInputsToState("external", logicalUnitInfo, out);

    out.unindent().println("}");
  }
  
  private void generateLogicalUnitUpdateMethod(LogicalUnitInfo logicalUnitInfo,
                                               IndentPrintWriter out) {
    out.println("@Override");
    out.printf("public void update(int %s) {%n", luIndexExpr).indent();

    out.printf("int luRow = %s / %d;%n", luIndexExpr, model.getHeight());
    out.printf("int luCol = %s %% %d;%n", luIndexExpr, model.getHeight());

    // Declare temporary variables.
    out.println("int tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7, tmp8;");

    // Copy state values into the appropriate variables.
    generateCopyStateValuesToVariables(logicalUnitInfo, out);
    out.println();

    // Precalculate indices into the external inputs array.
    generateExternalInputIndexVars("external", logicalUnitInfo, out);
    out.println();
    generateCopyStateValuesToExternalInputs("external", logicalUnitInfo, out);
    out.println();

    // Perform updates.

    generateConstants(logicalUnitInfo, out);
    generateUpdates(logicalUnitInfo, out);

    // Copy output values from variables back to state array.
    generateCopyVariablesToState(logicalUnitInfo, out);

    out.unindent().println("}");
  }

  @Override
  protected void printExternalCellDebugInfo(
      String outputVarName, String stateIndexExpr, IndentPrintWriter out) {
    out.printf("System.out.println(\"%s: %s = \" + (%s));",
        outputVarName, stateIndexExpr, outputVarName);
  }

  @Override
  protected void printUpdateDebugInfo(CellInfo cell, Output output, String updateExpr,
      LogicalUnitInfo logicalUnitInfo, IndentPrintWriter out) {
    out.printf("System.out.println(\"lu\" + luIndex + \":%s=\" + %s + \" %s\");%n",
        cell.getOutputVarName(output), cell.getOutputVarName(output), updateExpr);
    for (Input input : cell.getInputs()) {
      CellInfo inputCell = logicalUnitInfo.getCellInfo(input.getCell().getId());
      String inputVarName = inputCell.getOutputVarName(input.getOutput());
      out.printf("System.out.println(\"  %s = \" + %s);%n", inputVarName, inputVarName);
    }
  }
}
