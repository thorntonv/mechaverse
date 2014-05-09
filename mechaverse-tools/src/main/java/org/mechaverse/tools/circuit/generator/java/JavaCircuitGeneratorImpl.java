package org.mechaverse.tools.circuit.generator.java;

import java.io.PrintWriter;

import org.mechaverse.circuit.model.Output;
import org.mechaverse.circuit.model.Param;
import org.mechaverse.tools.circuit.generator.AbstractCircuitSimulationGenerator;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;

/**
 * A generator that creates Java source code for executing a circuit simulation.
 *
 * @author thorntonv@mechaverse.org
 */
public class JavaCircuitGeneratorImpl extends AbstractCircuitSimulationGenerator {

  public JavaCircuitGeneratorImpl(CircuitSimulationModel model) {
    super(model);
  }

  @Override
  public void generate(PrintWriter out) {
    // TODO(thorntonv): Properly indent the generated code.

    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();

    out.println("package org.mechaverse.tools.circuit.generator.java;");
    out.println("public class CircuitSimulationImpl implements CircuitSimulation {");
    out.println("@Override");
    out.println("public void update(int logicalUnitIndex, int state[]) {");

    // Copy state values into the appropriate external inputs.
    int idx = 0;
    for(ElementInfo element : logicalUnitInfo.getExternalElements()) {
      for(String outputVarName : element.getOutputVarNames()) {
        out.printf("int %s = 0;", outputVarName);
      }
      out.println();
    }
    out.println();

    // Copy state values into the appropriate variables.
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Output output : element.getOutputs()) {
        out.printf("int %s = state[%d];", element.getOutputVarName(output), idx++);
        out.println();
      }
    }
    out.println();
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Param param : element.getType().getParams()) {
        out.printf("int %s = state[%d];", element.getParamVarName(param), idx++);
        out.println();
      }
      for (Output output : element.getOutputs()) {
        for (Param param : output.getParams()) {
          out.printf("int %s = state[%d];", element.getOutputParamVarName(output, param), idx++);
          out.println();
        }
      }
      out.println();
    }

    // Perform updates.
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Output output : element.getOutputs()) {
        String expr = getVarMappedExpression(element, output);
        out.printf("%s = %s;", element.getOutputVarName(output), expr);
        out.println();
      }
    }

    out.println();

    // Copy output values from variables back to state array.
    idx = 0;
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Output output : element.getOutputs()) {
        out.printf("state[%d] = %s;", idx++, element.getOutputVarName(output));
        out.println();
      }
    }

    out.println("}");
    out.println("}");
    out.flush();
  }
}
