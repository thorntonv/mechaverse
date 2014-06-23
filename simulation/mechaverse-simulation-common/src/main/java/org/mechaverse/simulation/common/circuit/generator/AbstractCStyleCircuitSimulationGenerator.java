package org.mechaverse.simulation.common.circuit.generator;

import java.io.PrintWriter;

import org.mechaverse.circuit.model.Output;
import org.mechaverse.circuit.model.Param;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ExternalElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;

/**
 * An implementation of {@link CircuitSimulationGenerator} for C style languages.
 *
 * @author thorntonv@mechaverse.org
 */
public abstract class AbstractCStyleCircuitSimulationGenerator
    extends AbstractCircuitSimulationGenerator {

  protected static final String LU_INDEX_VAR_NAME = "luIndex";
  protected static final String STATE_VAR_NAME = "state";
  protected static final String LU_STATE_INDEX_VAR_NAME = "luStateIndex";


  public AbstractCStyleCircuitSimulationGenerator(CircuitSimulationModel model) {
    super(model);
  }

  protected void generateCopyStateValuesToExternalInputs(
      LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    for(ExternalElementInfo externalElement : logicalUnitInfo.getExternalElements()) {
      ElementInfo targetElement =
          logicalUnitInfo.getElementInfo(externalElement.getElement().getElementId());
      String targetVarName = targetElement.getOutputVarName(externalElement.getElement().getOutputId());
      int targetStateIndex = logicalUnitInfo.getStateIndex(targetVarName);
      for(String outputVarName : externalElement.getOutputVarNames()) {
        String stateIndexExpr = String.format("%s + (%d) + (%d)", LU_STATE_INDEX_VAR_NAME,
          getRelativeLogicalUnitIndex(externalElement.getElement()), targetStateIndex);
        out.printf("int %s = %s;", outputVarName, stateIndexExpr);
        out.println();
        out.printf("%s = %s[(%s > 0 ? %s : %s + %d) %% %d];", outputVarName, STATE_VAR_NAME,
            outputVarName, outputVarName, outputVarName, model.getCircuitStateSize(),
            model.getCircuitStateSize());
      }
      out.println();
    }
  }

  protected void generateCopyStateValuesToVariables(
      LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Output output : element.getOutputs()) {
        String varName = element.getOutputVarName(output);
        int stateIndex = logicalUnitInfo.getStateIndex(varName);
        out.println(loadStateToVarStatement(varName, stateIndex));
      }
    }
    out.println();
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Param param : element.getType().getParams()) {
        String varName = element.getParamVarName(param);
        int stateIndex = logicalUnitInfo.getStateIndex(varName);
        out.println(loadStateToVarStatement(varName, stateIndex));
      }
      for (Output output : element.getOutputs()) {
        for (Param param : output.getParams()) {
          String varName = element.getOutputParamVarName(output, param);
          int stateIndex = logicalUnitInfo.getStateIndex(varName);
          out.println(loadStateToVarStatement(varName, stateIndex));
        }
      }
      out.println();
    }
  }

  protected void generateUpdates(LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Output output : element.getOutputs()) {
        String expr = getVarMappedExpression(element, output);
        out.printf("%s = %s;", element.getOutputVarName(output), expr);
        out.println();
      }
    }
  }

  protected void generateCopyVariablesToState(LogicalUnitInfo logicalUnitInfo, PrintWriter out) {
    for (ElementInfo element : logicalUnitInfo.getElements()) {
      for (Output output : element.getOutputs()) {
        String varName = element.getOutputVarName(output);
        int stateIndex = logicalUnitInfo.getStateIndex(varName);
        out.println(saveVarToStateStatement(varName, stateIndex));
      }
    }
  }

  protected String loadStateToVarStatement(String varName, int stateIndex) {
    return String.format("int %s = %s[%s + %d];", varName, STATE_VAR_NAME,
        LU_STATE_INDEX_VAR_NAME, stateIndex);
  }

  protected String saveVarToStateStatement(String varName, int stateIndex) {
    return String.format("%s[%s + %d] = %s;", STATE_VAR_NAME, LU_STATE_INDEX_VAR_NAME,
        stateIndex, varName);
  }
}
