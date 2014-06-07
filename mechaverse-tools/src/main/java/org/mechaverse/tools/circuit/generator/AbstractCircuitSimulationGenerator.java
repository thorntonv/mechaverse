package org.mechaverse.tools.circuit.generator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mechaverse.circuit.model.Output;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.Input;

/**
 * A base class for {@link CircuitSimulationGenerator}s.
 *
 * @author thorntonv@mechaverse.org
 */
public abstract class AbstractCircuitSimulationGenerator implements CircuitSimulationGenerator {

  /**
   * A pattern that matches placeholder ids in an output expression.
   */
  protected static final Pattern EXPRESSION_VAR_PATTERN = Pattern.compile("\\{[^}]+\\}");

  private static final String ELEMENT_INPUT_ID_PREFIX = "input";

  protected final CircuitSimulationModel model;

  public AbstractCircuitSimulationGenerator(CircuitSimulationModel model) {
    this.model = model;
  }

  /**
   * @return the expression of the given output with placeholder ids replaced with variable names.
   */
  protected String getVarMappedExpression(ElementInfo element, Output output) {
    final String expression = output.getExpression().trim();
    Matcher matcher = EXPRESSION_VAR_PATTERN.matcher(expression);

    StringBuilder out = new StringBuilder();
    int position = 0;
    while (matcher.find()) {
      String placeholderId = matcher.group();
      // Remove enclosing curly braces.
      placeholderId = placeholderId.substring(1, placeholderId.length() - 1);

      // Attempt to map the id to an element parameter.
      String varName = element.getParamVarName(placeholderId);
      if (varName == null) {
        // Attempt to map the id to an element output parameter.
        varName = element.getOutputParamVarName(output, placeholderId);
      }
      if (varName == null && placeholderId.startsWith(ELEMENT_INPUT_ID_PREFIX)) {
        // Attempt to map the id to an element input.
        for (int idx = 0; idx < element.getInputs().length && varName == null; idx++) {
          Input input = element.getInputs()[idx];
          if (input != null && placeholderId.equalsIgnoreCase(ELEMENT_INPUT_ID_PREFIX + (idx+1))) {
            ElementInfo inputElement =
                model.getLogicalUnitInfo().getElementInfo(input.getElement().getId());
            varName = inputElement.getOutputVarName(input.getOutput());
          }
        }
      }
      if (varName == null) {
        throw new IllegalStateException("Unable to process element " + element.getId()
            + ": Invalid parameter " + placeholderId);
      }

      // Append the part of the expression between the last position and the start of the
      // placeholder id.
      out.append(expression.substring(position, matcher.start()));
      // Append the variable name to which the placeholder id was mapped.
      out.append(varName);
      // Set the current position the the end of the placeholder id.
      position = matcher.end();
    }
    // Append any remaining portion of the expression.
    out.append(expression.substring(position));
    return out.toString();
  }

  /**
   * Returns the index of the logical unit that is referenced by the given external element.
   */
  protected int getRelativeLogicalUnitIndex(ExternalElement element) {
    return (element.getRelativeUnitRow() * model.getWidth() + element.getRelativeUnitColumn())
        * model.getLogicalUnitInfo().getStateSize();
  }
}
