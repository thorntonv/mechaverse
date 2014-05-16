package org.mechaverse.tools.circuit.generator.java;

import java.io.PrintWriter;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.circuit.model.Output;
import org.mechaverse.circuit.model.Param;
import org.mechaverse.tools.circuit.generator.AbstractCircuitSimulationGenerator;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.ExternalElementInfo;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModelBuilder;

/**
 * A generator that creates Java source code for executing a circuit simulation.
 *
 * @author thorntonv@mechaverse.org
 */
public class JavaCircuitGeneratorImpl extends AbstractCircuitSimulationGenerator {

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

    out.println("package org.mechaverse.tools.circuit.generator.java;");
    out.println("public class CircuitSimulationImpl implements CircuitSimulation {");
    out.println("@Override");
    out.println("public void update(int logicalUnitIndex, int state[]) {");

    // Copy state values into the appropriate external inputs.
    for(ExternalElementInfo externalElement : logicalUnitInfo.getExternalElements()) {
      ElementInfo targetElement =
          logicalUnitInfo.getElementInfo(externalElement.getElement().getElementId());
      String targetVarName = targetElement.getOutputVarName(externalElement.getElement().getOutputId());
      int targetStateIndex = logicalUnitInfo.getStateIndex(targetVarName);
      for(String outputVarName : externalElement.getOutputVarNames()) {
        String stateIndexExpr = String.format("logicalUnitIndex * %d + (%d) + (%d)",
          model.getLogicalUnitInfo().getStateSize(),
          getRelativeLogicalUnitIndex(externalElement.getElement()), targetStateIndex);
        out.printf("int %s = %s;", outputVarName, stateIndexExpr);
        out.println();
        int totalSize = model.getLogicalUnitInfo().getStateSize() * model.getWidth() * model.getHeight();
        out.printf("%s = state[(%s > 0 ? %s : %s + %d) %% %d];",
          outputVarName, outputVarName, outputVarName, outputVarName, totalSize, totalSize);
      }
      out.println();
    }
    out.println();

    // Copy state values into the appropriate variables.
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Output output : element.getOutputs()) {
        String varName = element.getOutputVarName(output);
        int stateIndex = logicalUnitInfo.getStateIndex(varName);
        out.printf("int %s = state[%d];", varName, stateIndex);
        out.println();
      }
    }
    out.println();
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Param param : element.getType().getParams()) {
        String varName = element.getParamVarName(param);
        int stateIndex = logicalUnitInfo.getStateIndex(varName);
        out.printf("int %s = state[%d];", varName, stateIndex);
        out.println();
      }
      for (Output output : element.getOutputs()) {
        for (Param param : output.getParams()) {
          String varName = element.getOutputParamVarName(output, param);
          int stateIndex = logicalUnitInfo.getStateIndex(varName);
          out.printf("int %s = state[%d];", varName, stateIndex);
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
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Output output : element.getOutputs()) {
        String varName = element.getOutputVarName(output);
        int stateIndex = logicalUnitInfo.getStateIndex(varName);
        out.printf("state[%d] = %s;", stateIndex, varName);
        out.println();
      }
    }

    out.println("}");
    out.println("}");
    out.flush();
  }
}
