package org.mechaverse.simulation.common.circuit.generator.java;

import java.io.PrintWriter;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.circuit.model.Output;
import org.mechaverse.simulation.common.circuit.generator.AbstractCStyleCircuitSimulationGenerator;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.Input;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;

/**
 * A generator that creates Java source code for executing a circuit simulation.
 *
 * @author thorntonv@mechaverse.org
 */
public class JavaCircuitGeneratorImpl extends AbstractCStyleCircuitSimulationGenerator {

  // TODO(thorntonv): Generate circuit output code.

  public static final String TYPE = "java";

  static final String IMPL_PACKAGE = "org.mechaverse.simulation.common.circuit.generator.java";

  static final String IMPL_CLASS_NAME = "CircuitSimulationImpl";

  private final int circuitInputSize;

  public JavaCircuitGeneratorImpl(Circuit circuit, int circuitInputSize) {
    this(new CircuitSimulationModelBuilder().buildModel(circuit), circuitInputSize);
  }

  public JavaCircuitGeneratorImpl(CircuitSimulationModel model, int circuitInputSize) {
    super(model);
    this.circuitInputSize = circuitInputSize;
  }

  @Override
  protected String getLogicalUnitIndexExpr() {
    return "luIndex";
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
    out.printf("super(%d, %d, %d, %d, %d);%n", model.getLogicalUnitCount(), numExternalElements,
        model.getCircuitStateSize(), circuitInputSize, model.getIterationsPerUpdate());
    out.println("}");
  }

  private void generateLogicalUnitUpdateMethod(LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    out.println("@Override");
    out.printf("public void update(int %s) {%n", luIndexExpr);

    // Copy state values into the appropriate variables.
    generateCopyStateValuesToVariables(logicalUnitInfo, out);
    out.println();

    generateCopyStateValuesToExternalInputs("external", logicalUnitInfo, out);
    out.println();

    generateConstants(logicalUnitInfo, out);

    // Perform updates.

    generateUpdates(logicalUnitInfo, out);

    // Copy output values from variables back to state array.
    generateCopyVariablesToState(logicalUnitInfo, out);

    out.println("}");
  }

  @Override
  protected void printExternalElementDebugInfo(
      String outputVarName, String stateIndexExpr, PrintWriter out) {
    out.printf("System.out.println(\"%s: %s = \" + (%s));",
        outputVarName, stateIndexExpr, outputVarName);
  }

  @Override
  protected void printUpdateDebugInfo(ElementInfo element, Output output, String updateExpr,
      LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    out.printf("System.out.println(\"lu\" + luIndex + \":%s=\" + %s + \" %s\");%n",
        element.getOutputVarName(output), element.getOutputVarName(output), updateExpr);
    for (Input input : element.getInputs()) {
      ElementInfo inputElement = logicalUnitInfo.getElementInfo(input.getElement().getId());
      String inputVarName = inputElement.getOutputVarName(input.getOutput());
      out.printf("System.out.println(\"  %s = \" + %s);%n", inputVarName, inputVarName);
    }
  }
}
