package org.mechaverse.simulation.common.cellautomaton.simulation;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import org.mechaverse.cellautomaton.model.Output;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.CellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.ExternalCellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.Input;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.ExternalCell;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

public class CellularAutomatonTestUtil {

  public static class CellInfoVerifier {

    private CellInfo cellInfo;

    public CellInfoVerifier(CellInfo cellInfo) {
      this.cellInfo = cellInfo;
    }

    public CellInfoVerifier verifyId(String id) {
      assertEquals(id, cellInfo.getId());
      return this;
    }

    public CellInfoVerifier verifyType(String type) {
      assertEquals(type, cellInfo.getCell().getType());
      return this;
    }

    public CellInfoVerifier verifyInputCount(int count) {
      assertEquals(count, cellInfo.getInputs().length);
      return this;
    }

    public CellInfoVerifier verifyInput(
        int inputIdx, CellInfo expectedCell, Output expectedOutput) {
      Input input = cellInfo.getInputs()[inputIdx];
      assertEquals(expectedCell.getCell(), input.getCell());
      assertEquals(expectedOutput, input.getOutput());
      return this;
    }

    public CellInfoVerifier verifyExternalInput(int inputIdx, String expectedId) {
      Input input = cellInfo.getInputs()[inputIdx];
      assertEquals(ExternalCell.TYPE, input.getCell().getType());
      assertEquals(expectedId, input.getCell().getId());
      return this;
    }

    public CellInfoVerifier verifyInputIds(String... ids) {
      assertEquals(ids.length, cellInfo.getInputs().length);
      for (int idx = 0; idx < ids.length; idx++) {
        assertEquals(ids[idx], cellInfo.getInputs()[idx].getCell().getId());
      }
      return this;
    }

    public CellInfoVerifier verifyOutputVarNames(String... varNames) {
      assertEquals(varNames.length, cellInfo.getOutputs().size());
      assertEquals(varNames.length, cellInfo.getOutputVarNames().size());
      for (int idx = 0; idx < varNames.length; idx++) {
        assertEquals(varNames[idx], cellInfo.getOutputVarName(cellInfo.getOutputs().get(idx)));
      }
      return this;
    }

    /**
     * Verifies output parameter variable names. Matrix has the following structure:
     * <pre>
     * {
     * // Output 1
     * { {"paramId", "paramVarName"}, {"paramId", "paramVarName"}, ... }
     * // Output 2
     * { {"paramId", "paramVarName"}, {"paramId", "paramVarName"}, ... }
     * ...
     * }
     * </pre>
     */
    public CellInfoVerifier verifyOutputParamVarNames(String[][][] matrix) {
      for (int outputIdx = 0; outputIdx < matrix.length; outputIdx++) {
        verifyOutputParamVarNames(outputIdx, matrix[outputIdx]);
      }
      return this;
    }

    public CellInfoVerifier verifyOutputParamVarNames(int outputIdx, String[][] matrix) {
      for (String[] row : matrix) {
        assertEquals(row[1], cellInfo.getOutputParamVarName(
            cellInfo.getOutputs().get(outputIdx), row[0]));
      }
      return this;
    }

    public static CellInfoVerifier of(CellInfo cellInfo) {
      return new CellInfoVerifier(cellInfo);
    }
  }

  public static class ExternalCellInfoVerifier {

    private ExternalCell cell;

    public ExternalCellInfoVerifier(ExternalCellInfo cellInfo) {
      this.cell = cellInfo.getCell();
    }

    public ExternalCellInfoVerifier verifyRelativeRow(int relativeRow) {
      assertEquals(relativeRow, cell.getRelativeUnitRow());
      return this;
    }

    public ExternalCellInfoVerifier verifyRelativeColumn(int relativeColumn) {
      assertEquals(relativeColumn, cell.getRelativeUnitColumn());
      return this;
    }

    public ExternalCellInfoVerifier verifyCellId(String id) {
      assertEquals(id, cell.getCellId());
      return this;
    }

    public ExternalCellInfoVerifier verifyOutputId(String id) {
      assertEquals(id, cell.getOutputId());
      return this;
    }

    public static ExternalCellInfoVerifier of(ExternalCellInfo cellInfo) {
      return new ExternalCellInfoVerifier(cellInfo);
    }
  }

  /**
   * Sets the given state to a repeatable random state.
   */
  public static void setRandomState(int[] state) {
    Random random = new Random(CellularAutomatonTestUtil.class.getName().hashCode());
    for (int idx = 0; idx < state.length; idx++) {
      state[idx] = random.nextInt();
    }
  }

  /**
   * Returns an {@link Answer} that copies the given data to the passed argument.
   */
  public static Answer<Void> mockIntArrayAnswer(
      final int[] data, final ArgumentCaptor<int[]> argCaptor) {
    return invocation -> {
      System.arraycopy(data, 0, argCaptor.getValue(), 0, data.length);
      return null;
    };
  }
}
