package org.mechaverse.simulation.common.circuit;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.mechaverse.circuit.model.Output;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ExternalElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.Input;
import org.mechaverse.simulation.common.circuit.generator.ExternalElement;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CircuitTestUtil {

  public static class ElementInfoVerifier {

    private ElementInfo elementInfo;

    public ElementInfoVerifier(ElementInfo elementInfo) {
      this.elementInfo = elementInfo;
    }

    public ElementInfoVerifier verifyId(String id) {
      assertEquals(id, elementInfo.getId());
      return this;
    }

    public ElementInfoVerifier verifyType(String type) {
      assertEquals(type, elementInfo.getElement().getType());
      return this;
    }

    public ElementInfoVerifier verifyInputCount(int count) {
      assertEquals(count, elementInfo.getInputs().length);
      return this;
    }

    public ElementInfoVerifier verifyInput(int inputIdx, ElementInfo expectedElement,
        Output expectedOutput) {
      Input input = elementInfo.getInputs()[inputIdx];
      assertEquals(expectedElement.getElement(), input.getElement());
      assertEquals(expectedOutput, input.getOutput());
      return this;
    }

    public ElementInfoVerifier verifyExternalInput(int inputIdx, String expectedId) {
      Input input = elementInfo.getInputs()[inputIdx];
      assertEquals(ExternalElement.TYPE, input.getElement().getType());
      assertEquals(expectedId, input.getElement().getId());
      return this;
    }

    public ElementInfoVerifier verifyInputIds(String... ids) {
      assertEquals(ids.length, elementInfo.getInputs().length);
      for (int idx = 0; idx < ids.length; idx++) {
        assertEquals(ids[idx], elementInfo.getInputs()[idx].getElement().getId());
      }
      return this;
    }

    public ElementInfoVerifier verifyOutputVarNames(String... varNames) {
      assertEquals(varNames.length, elementInfo.getOutputs().size());
      assertEquals(varNames.length, elementInfo.getOutputVarNames().size());
      for (int idx = 0; idx < varNames.length; idx++) {
        assertEquals(varNames[idx], elementInfo.getOutputVarName(
          elementInfo.getOutputs().get(idx)));
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
    public ElementInfoVerifier verifyOutputParamVarNames(String[][][] matrix) {
      for (int outputIdx = 0; outputIdx < matrix.length; outputIdx++) {
        verifyOutputParamVarNames(outputIdx, matrix[outputIdx]);
      }
      return this;
    }

    public ElementInfoVerifier verifyOutputParamVarNames(int outputIdx, String[][] matrix) {
      for (int idx = 0; idx < matrix.length; idx++) {
        assertEquals(matrix[idx][1], elementInfo.getOutputParamVarName(
            elementInfo.getOutputs().get(outputIdx), matrix[idx][0]));
      }
      return this;
    }

    public static ElementInfoVerifier of(ElementInfo elementInfo) {
      return new ElementInfoVerifier(elementInfo);
    }
  }

  public static class ExternalElementInfoVerifier {

    private ExternalElement element;

    public ExternalElementInfoVerifier(ExternalElementInfo elementInfo) {
      this.element = elementInfo.getElement();
    }

    public ExternalElementInfoVerifier verifyRelativeRow(int relativeRow) {
      assertEquals(relativeRow, element.getRelativeUnitRow());
      return this;
    }

    public ExternalElementInfoVerifier verifyRelativeColumn(int relativeColumn) {
      assertEquals(relativeColumn, element.getRelativeUnitColumn());
      return this;
    }

    public ExternalElementInfoVerifier verifyElementId(String id) {
      assertEquals(id, element.getElementId());
      return this;
    }

    public ExternalElementInfoVerifier verifyOutputId(String id) {
      assertEquals(id, element.getOutputId());
      return this;
    }

    public static ExternalElementInfoVerifier of(ExternalElementInfo elementInfo) {
      return new ExternalElementInfoVerifier(elementInfo);
    }
  }

  /**
   * Sets the given state to a repeatable random state.
   */
  public static void setRandomState(int[] state) {
    Random random = new Random(CircuitTestUtil.class.getName().hashCode());
    for (int idx = 0; idx < state.length; idx++) {
      state[idx] = random.nextInt();
    }
  }

  /**
   * Returns an {@link Answer} that copies the given data to the passed argument.
   */
  public static Answer<Void> mockIntArrayAnswer(
      final int[] data, final ArgumentCaptor<int[]> argCaptor) {
    return new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        System.arraycopy(data, 0, argCaptor.getValue(), 0, data.length);
        return null;
      }
    };
  }
}
