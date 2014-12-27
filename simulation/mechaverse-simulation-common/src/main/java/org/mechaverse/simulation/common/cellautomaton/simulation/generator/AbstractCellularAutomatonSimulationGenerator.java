package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mechaverse.cellautomaton.model.Output;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.CellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.Input;


/**
 * A base class for {@link CellularAutomatonSimulationGenerator}s.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class AbstractCellularAutomatonSimulationGenerator
    implements CellularAutomatonSimulationGenerator {

  /**
   * A pattern that matches placeholder ids in an output expression.
   */
  protected static final Pattern EXPRESSION_VAR_PATTERN = Pattern.compile("\\{[^}]+\\}");

  private static final String CELL_INPUT_ID_PREFIX = "input";
  private static final String CELL_OUTPUT_ID_PREFIX = "output";

  protected final CellularAutomatonSimulationModel model;

  public AbstractCellularAutomatonSimulationGenerator(CellularAutomatonSimulationModel model) {
    this.model = model;
  }

  /**
   * @return the expression of the given output with placeholder ids replaced with variable names.
   */
  protected String getVarMappedString(String str, CellInfo cell, Output output) {
    str = str.trim();
    Matcher matcher = EXPRESSION_VAR_PATTERN.matcher(str);

    StringBuilder out = new StringBuilder();
    int position = 0;
    while (matcher.find()) {
      String placeholderId = matcher.group();
      // Remove enclosing curly braces.
      placeholderId = placeholderId.substring(1, placeholderId.length() - 1);

      // Attempt to map the id to a cell parameter.
      String varName = cell.getParamVarName(placeholderId);
      if (varName == null) {
        // Attempt to map the id to a cell output parameter.
        varName = cell.getOutputParamVarName(output, placeholderId);
      }
      if (varName == null && placeholderId.startsWith(CELL_INPUT_ID_PREFIX)) {
        // Attempt to map the id to a cell input.
        for (int idx = 0; idx < cell.getInputs().length && varName == null; idx++) {
          Input input = cell.getInputs()[idx];
          if (input != null && placeholderId.equalsIgnoreCase(CELL_INPUT_ID_PREFIX + (idx+1))) {
            CellInfo inputCell =
                model.getLogicalUnitInfo().getCellInfo(input.getCell().getId());
            varName = inputCell.getOutputVarName(input.getOutput());
          }
        }
      }
      if (varName == null && placeholderId.startsWith(CELL_OUTPUT_ID_PREFIX)) {
        // Attempt to map the id to a cell output.
        for (int idx = 0; idx < cell.getOutputs().size() && varName == null; idx++) {
          if (placeholderId.equalsIgnoreCase(CELL_OUTPUT_ID_PREFIX + (idx + 1))) {
            varName = cell.getOutputVarName(cell.getOutputs().get(idx));
          }
        }
      }
      if (varName == null) {
        throw new IllegalStateException("Unable to process cell " + cell.getId()
            + ": Invalid parameter " + placeholderId);
      }

      // Append the part of the expression between the last position and the start of the
      // placeholder id.
      out.append(str.substring(position, matcher.start()));
      // Append the variable name to which the placeholder id was mapped.
      out.append(varName);
      // Set the current position the the end of the placeholder id.
      position = matcher.end();
    }
    // Append any remaining portion of the string.
    out.append(str.substring(position));
    return out.toString();
  }

  /**
   * Returns the index of the logical unit that is referenced by the given external cell relative
   * to the index of the current logical unit.
   */
  protected int getRelativeLogicalUnitIndex(ExternalCell cell) {
    return cell.getRelativeUnitRow() * model.getWidth() + cell.getRelativeUnitColumn();
  }
}
