package org.mechaverse.simulation.common.opencl;

import java.io.PrintWriter;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.generator.AbstractCStyleCircuitSimulationGenerator;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;

/**
 * Generates a circuit simulation kernel that can be executed using OpenCL devices.
 *
 * @author thorntonv@mechaverse.org
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
  public void generate(PrintWriter out) {
    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();

    out.println("void kernel " + OpenClCircuitSimulator.KERNEL_NAME + "(");
    out.println("global const int* input, global int* state, global int* output) {");

    out.printf("int %s = get_local_id(0) * %d;\n", LU_STATE_INDEX_VAR_NAME,
      model.getLogicalUnitInfo().getStateSize());

    // Copy state values into the appropriate external inputs.
    generateCopyStateValuesToExternalInputs(logicalUnitInfo, out);
    out.println();

    // Copy state values into the appropriate variables.
    generateCopyStateValuesToVariables(logicalUnitInfo, out);
    out.println();

    // Perform updates.
    generateUpdates(logicalUnitInfo, out);
    out.println();

    // Copy output values from variables back to state array.
    generateCopyVariablesToState(logicalUnitInfo, out);

    out.println("}");
  }
}
