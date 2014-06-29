package org.mechaverse.simulation.common.circuit.generator.java;

import java.io.PrintWriter;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.generator.AbstractCStyleCircuitSimulationGenerator;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;

/**
 * A generator that creates Java source code for executing a circuit simulation.
 *
 * @author thorntonv@mechaverse.org
 */
public class JavaCircuitGeneratorImpl extends AbstractCStyleCircuitSimulationGenerator {

  public static final String TYPE = "java";

  static final String IMPL_PACKAGE = "org.mechaverse.simulation.common" +
      ".circuit.generator.java";

  static final String IMPL_CLASS_NAME = "CircuitSimulationImpl";

  public JavaCircuitGeneratorImpl(Circuit circuit) {
    this(new CircuitSimulationModelBuilder().buildModel(circuit));
  }

  public JavaCircuitGeneratorImpl(CircuitSimulationModel model) {
    super(model);
  }

  @Override
  public void generate(PrintWriter out) {
    // TODO(thorntonv): Properly indent the generated code.

    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();

    out.printf("package %s;%n", IMPL_PACKAGE);
    out.println("public class CircuitSimulationImpl extends AbstractJavaCircuitSimulationImpl {");
    generateConstructor(logicalUnitInfo, out);
    generateLogicalUnitUpdateMethod(logicalUnitInfo, out);
    out.println("}");
    out.flush();
  }

  private void generateConstructor(LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    out.println("public CircuitSimulationImpl() {");
    out.printf("super(%d, %d);%n", model.getLogicalUnitCount(), model.getCircuitStateSize());
    out.println("}");
  }

  private void generateLogicalUnitUpdateMethod(LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    out.println("@Override");
    out.printf("public void update(int %s) {%n", LU_INDEX_VAR_NAME);

    out.printf("int %s = %s * %d;%n", LU_STATE_INDEX_VAR_NAME, LU_INDEX_VAR_NAME,
      logicalUnitInfo.getStateSize());

    // Copy state values into the appropriate external inputs.
    generateCopyStateValuesToExternalInputs(logicalUnitInfo, out);
    out.println();

    // Copy state values into the appropriate variables.
    generateCopyStateValuesToVariables(logicalUnitInfo, out);
    out.println();

    // Perform updates.

    if (model.getIterationsPerUpdate() > 1) {
      out.println("for(int cnt = 0; cnt < " + model.getIterationsPerUpdate() + "; cnt++) {");
    }
    generateUpdates(logicalUnitInfo, out);
    if (model.getIterationsPerUpdate() > 1) {
      out.println("}");
    }

    // Copy output values from variables back to state array.
    generateCopyVariablesToState(logicalUnitInfo, out);

    out.println("}");
  }
}
