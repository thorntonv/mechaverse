package org.mechaverse.simulation.common.opencl;

import java.io.PrintWriter;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.circuit.model.Output;
import org.mechaverse.simulation.common.circuit.generator.AbstractCStyleCircuitSimulationGenerator;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;

/**
 * Generates a circuit simulation kernel that can be executed using OpenCL devices.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class OpenClCircuitGeneratorImpl extends AbstractCStyleCircuitSimulationGenerator {

  public static final String TYPE = "opencl";

  public OpenClCircuitGeneratorImpl(Circuit circuit) {
    this(new CircuitSimulationModelBuilder().buildModel(circuit));
  }

  public OpenClCircuitGeneratorImpl(CircuitSimulationModel model) {
    super(model);
  }

  @Override
  protected String getLogicalUnitIndexExpr() {
    return "get_local_id(0)";
  }

  @Override
  public void generate(PrintWriter out) {
    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();
    int numExternalElements = logicalUnitInfo.getExternalElements().size();

    out.println("void kernel " + OpenClCircuitSimulator.KERNEL_NAME + "(");
    out.println("global const int* circuitInputs, global int* circuitStates, "
        + "global int* circuitOutputMaps, global int* circuitOutputs, "
        + "const unsigned int circuitInputLength, const unsigned int circuitOutputLength) {");

    String circuitIdxExpr = "get_global_id(0) / get_local_size(0)";

    // State is arranged as an array of circuit states. The state of each circuit is arranged as
    // an array of logical unit states. The circuit index is get_global_id(0) / get_local_size(0)
    out.printf("__global int* circuitState = &circuitStates[%s * %d];%n",
        circuitIdxExpr, model.getCircuitStateSize());

    out.printf("__global int* circuitInput = &circuitInputs[%s * circuitInputLength];%n",
        circuitIdxExpr);

    out.printf("__global int* circuitOutputMap = &circuitOutputMaps[%s * circuitOutputLength];%n",
        circuitIdxExpr);

    out.printf("__global int* circuitOutput = &circuitOutputs[%s * circuitOutputLength];%n",
        circuitIdxExpr);

    // Copy state values into the appropriate variables.
    generateCopyStateValuesToVariables(logicalUnitInfo, out);
    out.println();

    out.printf("__local int external[%d];%n", model.getLogicalUnitCount() * numExternalElements);

    // Copy external elements to shared memory.
    generateCopyExternalInputsToState("external", logicalUnitInfo, out);
    generateBarrier(out);
    out.println();

    // Generate constants.
    generateConstants(logicalUnitInfo, out);
    out.println();

    // Perform updates.

    if (model.getIterationsPerUpdate() > 1) {
      out.println("for(int cnt = 0; cnt < " + model.getIterationsPerUpdate() + "; cnt++) {");
    }

    // Copy state values into the appropriate external inputs.
    generateCopyStateValuesToExternalInputs("external", logicalUnitInfo, out);
    out.println();

    generateUpdates(logicalUnitInfo, out);

    generateBarrier(out);
    generateCopyExternalInputsToState("external", logicalUnitInfo, out);
    generateBarrier(out);

    if (model.getIterationsPerUpdate() > 1) {
      out.println("}");
    }

    // Copy output values from variables back to state array.
    generateCopyVariablesToState(logicalUnitInfo, out);
    out.println();

    generateCopyStateValuesToOutput(out);

    out.println("}");
  }

  protected void generateBarrier(PrintWriter out) {
    out.println("barrier(CLK_LOCAL_MEM_FENCE);");
  }

  protected void generateCopyStateValuesToOutput(PrintWriter out) {
    // Each local thread is responsible for copying the indices associated with its id.
    out.println("for(int idx = get_local_id(0); idx < circuitOutputLength; "
        + "idx += get_local_size(0)) {");
    out.println("circuitOutput[idx] = circuitState[circuitOutputMap[idx]];");
    out.println("}");
  }

  @Override
  protected void printUpdateDebugInfo(ElementInfo element, Output output, String updateExpr,
      LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    // TODO(thorntonv): Determine if this method can be implemented.
  }

  @Override
  protected void printExternalElementDebugInfo(String outputVarName, String stateIndexExpr,
      PrintWriter out) {
    // TODO(thorntonv): Determine if this method can be implemented.
  }
}
