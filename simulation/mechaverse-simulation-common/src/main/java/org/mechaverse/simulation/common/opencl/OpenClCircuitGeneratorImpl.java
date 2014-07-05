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
 * @author thorntonv@mechaverse.org
 */
public class OpenClCircuitGeneratorImpl extends AbstractCStyleCircuitSimulationGenerator {

  public static final String TYPE = "opencl";

  private static final String CIRCUIT_THREAD_OFFSET_VAR_NAME = "circuitStateOffset";

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
  protected String getThreadStateOffsetExpr() {
    return CIRCUIT_THREAD_OFFSET_VAR_NAME;
  }

  @Override
  public void generate(PrintWriter out) {
    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();
    int numExternalElements = logicalUnitInfo.getExternalElements().size();

    out.println("void kernel " + OpenClCircuitSimulator.KERNEL_NAME + "(");
    out.println("global const int* input, global int* state, global int* output) {");

    out.printf("int %s = get_global_id(0) / get_local_size(0) * %d + get_local_id(0);%n",
      CIRCUIT_THREAD_OFFSET_VAR_NAME, model.getCircuitStateSize());

    // Copy state values into the appropriate variables.
    generateCopyStateValuesToVariables(logicalUnitInfo, out);
    out.println();

    out.printf("__local int external[%d];%n", model.getLogicalUnitCount() * numExternalElements);

    // Copy external elements to shared memory.
    generateCopyExternalInputsToState("external", logicalUnitInfo, out);
    generateBarrier(out);

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

    out.println("}");
  }

  protected void generateBarrier(PrintWriter out) {
    out.println("barrier(CLK_LOCAL_MEM_FENCE);");
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
