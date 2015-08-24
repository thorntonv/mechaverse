package org.mechaverse.simulation.common.cellautomaton.simulation.opencl;

import java.io.PrintWriter;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.cellautomaton.model.Output;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.AbstractCStyleSimulationGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.CellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.LogicalUnitInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.mechaverse.simulation.common.util.IndentPrintWriter;

/**
 * Generates a cellular automaton simulation kernel that can be executed using OpenCL devices.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class OpenClCellularAutomatonGeneratorImpl extends AbstractCStyleSimulationGenerator {

  public static final String TYPE = "opencl";

  public OpenClCellularAutomatonGeneratorImpl(CellularAutomatonDescriptor descriptor) {
    this(new CellularAutomatonSimulationModelBuilder().buildModel(descriptor));
  }

  public OpenClCellularAutomatonGeneratorImpl(CellularAutomatonSimulationModel model) {
    super(model);
  }

  @Override
  protected String getLogicalUnitIndexExpr() {
    return "get_local_id(0)";
  }

  @Override
  public void generate(final PrintWriter printWriter) {
    final IndentPrintWriter out = new IndentPrintWriter(printWriter);

    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();
    int numExternalCells = logicalUnitInfo.getExternalCells().size();

    out.println("void kernel " + OpenClCellularAutomatonSimulator.KERNEL_NAME + "(");
    out.println("global const int* automatonInputMaps, global const int* automatonInputs, " +
        "global int* automatonStates, global int* automatonOutputMaps, " +
        "global int* automatonOutputs, const unsigned int automatonInputLength, " +
        "const unsigned int automatonOutputLength) {").indent();

    String automatonIdxExpr = "get_global_id(0) / get_local_size(0)";

    // State is arranged as an array of automaton states. The state of each automaton is arranged as
    // an array of logical unit states. The automaton index is get_global_id(0) / get_local_size(0)
    out.printf("__global int* automatonState = &automatonStates[%s * %d];%n",
        automatonIdxExpr, model.getStateSize());

    out.printf(
        "__global int* automatonInputMap = &automatonInputMaps[%s * automatonInputLength];%n",
        automatonIdxExpr);

    out.printf(
        "__global int* automatonInput = &automatonInputs[%s * automatonInputLength];%n",
            automatonIdxExpr);

    out.printf(
        "__global int* automatonOutputMap = &automatonOutputMaps[%s * automatonOutputLength];%n",
            automatonIdxExpr);

    out.printf("__global int* automatonOutput = &automatonOutputs[%s * automatonOutputLength];%n",
        automatonIdxExpr);

    out.println();

    out.printf("int luRow = %s / %d;%n", luIndexExpr, model.getHeight());
    out.printf("int luCol = %s %% %d;%n", luIndexExpr, model.getHeight());
    out.println();

    generateCopyInputValuesToState(out);
    out.println();

    // Copy state values into the appropriate variables.
    generateCopyStateValuesToVariables(logicalUnitInfo, out);
    out.println();

    out.printf("__local int external[%d];%n", model.getLogicalUnitCount() * numExternalCells);

    // Declare temporary variables.
    out.println("int tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7, tmp8;");

    // Copy external cells to shared memory.
    generateCopyExternalInputsToState("external", logicalUnitInfo, out);
    generateBarrier(out);
    out.println();

    // Precalculate indices into the external inputs array.
    generateExternalInputIndexVars("external", logicalUnitInfo, out);
    out.println();

    generateConstants(logicalUnitInfo, out);
    out.println();

    // Perform updates.
    
    if (model.getIterationsPerUpdate() > 1) {
      out.println("for(int cnt = 0; cnt < " + model.getIterationsPerUpdate() + "; cnt++) {")
        .indent();
    }

    // Copy state values into the appropriate external inputs.
    generateCopyStateValuesToExternalInputs("external", logicalUnitInfo, out);
    out.println();

    generateUpdates(logicalUnitInfo, out);

    generateBarrier(out);
    generateCopyExternalInputsToState("external", logicalUnitInfo, out);
    generateBarrier(out);

    if (model.getIterationsPerUpdate() > 1) {
      out.unindent().println("}");
    }

    // Copy output values from variables back to state array.
    generateCopyVariablesToState(logicalUnitInfo, out);
    out.println();

    generateCopyStateValuesToOutput(out);

    out.unindent().println("}");
  }

  protected void generateBarrier(IndentPrintWriter out) {
    out.println("barrier(CLK_LOCAL_MEM_FENCE);");
  }

  protected void generateCopyInputValuesToState(IndentPrintWriter out) {
    // Each local thread is responsible for copying the indices associated with its id.
    out.println("for(int idx = get_local_id(0); idx < automatonInputLength; "
        + "idx += get_local_size(0)) {").indent();
    out.println("automatonState[automatonInputMap[idx]] = automatonInput[idx];");
    out.unindent().println("}");
  }

  protected void generateCopyStateValuesToOutput(IndentPrintWriter out) {
    // Each local thread is responsible for copying the indices associated with its id.
    out.println("for(int idx = get_local_id(0); idx < automatonOutputLength; "
        + "idx += get_local_size(0)) {").indent();
    out.println("automatonOutput[idx] = automatonState[automatonOutputMap[idx]];");
    out.unindent().println("}");
  }

  @Override
  protected void printUpdateDebugInfo(CellInfo cellInfo, Output output, String updateExpr,
      LogicalUnitInfo logicalUnitInfo, IndentPrintWriter out) {
    // TODO(thorntonv): Determine if this method can be implemented.
  }

  @Override
  protected void printExternalCellDebugInfo(String outputVarName, String stateIndexExpr,
                                            IndentPrintWriter out) {
    // TODO(thorntonv): Determine if this method can be implemented.
  }
}
