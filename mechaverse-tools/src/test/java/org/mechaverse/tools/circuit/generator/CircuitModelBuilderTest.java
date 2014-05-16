package org.mechaverse.tools.circuit.generator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.tools.circuit.CircuitTestUtil;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.ExternalElementInfo;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;

/**
 * Unit test {@link CircuitSimulationModelBuilder}.
 *
 * @author thorntonv@mechaverse.org
 */
public class CircuitModelBuilderTest {

  @Test
  public void testBuildModel() {
    Circuit testCircuit1 = CircuitTestUtil.createTestCircuit1();
    CircuitSimulationModelBuilder modelBuilder = new CircuitSimulationModelBuilder();
    CircuitSimulationModel model = modelBuilder.buildModel(testCircuit1);
    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();
    assertEquals(2, logicalUnitInfo.getElements().size());
    assertEquals(4, logicalUnitInfo.getExternalElements().size());
    // 9 values per element * 2 elements.
    assertEquals(9*2, logicalUnitInfo.getStateSize());

    // External elements.
    verifyExternalElement(logicalUnitInfo.getExternalElementInfo("in1"), 0, -1, "2", "3");
    verifyExternalElement(logicalUnitInfo.getExternalElementInfo("in2"), -1, 0, "1", "2");
    verifyExternalElement(logicalUnitInfo.getExternalElementInfo("in3"), 1, 0, "2", "2");
    verifyExternalElement(logicalUnitInfo.getExternalElementInfo("in4"), 0, 1, "1", "1");

    // Elements.

    ElementInfo element1 = logicalUnitInfo.getElementInfo("1");
    ElementInfo element2 = logicalUnitInfo.getElementInfo("2");

    // Element 1.
    assertEquals(CircuitTestUtil.ROUTING_3IN3OUT_TYPE, element1.getElement().getType());
    assertEquals(3, element1.getInputs().length);
    assertEquals("in1", element1.getInputs()[0].getElement().getId());
    assertEquals("in2", element1.getInputs()[1].getElement().getId());
    assertEquals(element2.getElement(), element1.getInputs()[2].getElement());
    assertEquals(3, element1.getOutputs().size());
    assertEquals(3, element1.getOutputVarNames().size());
    assertEquals("e1_out1", element1.getOutputVarName(element1.getOutputs().get(0)));
    assertEquals("e1_out2", element1.getOutputVarName(element1.getOutputs().get(1)));
    assertEquals("e1_out3", element1.getOutputVarName(element1.getOutputs().get(2)));
    assertEquals(6, element1.getParamVarNames().size());
    assertEquals("e1_out1_input2Mask",
        element1.getOutputParamVarName(element1.getOutputs().get(0), "input2Mask"));
    assertEquals("e1_out1_input3Mask",
        element1.getOutputParamVarName(element1.getOutputs().get(0), "input3Mask"));
    assertEquals("e1_out2_input1Mask",
        element1.getOutputParamVarName(element1.getOutputs().get(1), "input1Mask"));
    assertEquals("e1_out2_input3Mask",
        element1.getOutputParamVarName(element1.getOutputs().get(1), "input3Mask"));
    assertEquals("e1_out3_input1Mask",
        element1.getOutputParamVarName(element1.getOutputs().get(2), "input1Mask"));
    assertEquals("e1_out3_input2Mask",
        element1.getOutputParamVarName(element1.getOutputs().get(2), "input2Mask"));

    // Element 2.
    assertEquals(CircuitTestUtil.ROUTING_3IN3OUT_TYPE, element2.getElement().getType());
    assertEquals(3, element2.getInputs().length);
    assertEquals(element1.getElement(), element2.getInputs()[0].getElement());
    assertEquals("in3", element2.getInputs()[1].getElement().getId());
    assertEquals("in4", element2.getInputs()[2].getElement().getId());
    assertEquals(3, element2.getOutputs().size());
    assertEquals(3, element2.getOutputVarNames().size());
    assertEquals("e2_out1", element2.getOutputVarName(element1.getOutputs().get(0)));
    assertEquals("e2_out2", element2.getOutputVarName(element1.getOutputs().get(1)));
    assertEquals("e2_out3", element2.getOutputVarName(element1.getOutputs().get(2)));
    assertEquals(6, element2.getParamVarNames().size());
    assertEquals("e2_out1_input2Mask",
      element2.getOutputParamVarName(element1.getOutputs().get(0), "input2Mask"));

    // External element 1.
    ExternalElement externalElement1 = logicalUnitInfo.getExternalElementInfo("in1").getElement();
    assertEquals(-1, externalElement1.getRelativeUnitColumn());
    assertEquals(0, externalElement1.getRelativeUnitRow());
    assertEquals("2", externalElement1.getElementId());
    assertEquals("3", externalElement1.getOutputId());

    // External element 2.
    ExternalElement externalElement2 = logicalUnitInfo.getExternalElementInfo("in2").getElement();
    assertEquals(0, externalElement2.getRelativeUnitColumn());
    assertEquals(-1, externalElement2.getRelativeUnitRow());
    assertEquals("1", externalElement2.getElementId());
    assertEquals("2", externalElement2.getOutputId());

    // External element 3.
    ExternalElement externalElement3 = logicalUnitInfo.getExternalElementInfo("in3").getElement();
    assertEquals(0, externalElement3.getRelativeUnitColumn());
    assertEquals(1, externalElement3.getRelativeUnitRow());
    assertEquals("2", externalElement3.getElementId());
    assertEquals("2", externalElement3.getOutputId());

    // External element 4.
    ExternalElement externalElement4 = logicalUnitInfo.getExternalElementInfo("in4").getElement();
    assertEquals(1, externalElement4.getRelativeUnitColumn());
    assertEquals(0, externalElement4.getRelativeUnitRow());
    assertEquals("1", externalElement4.getElementId());
    assertEquals("1", externalElement4.getOutputId());
  }

  private void verifyExternalElement(ExternalElementInfo externalElementInfo, int relativeUnitRow,
      int relativeUnitColumn, String elementId, String outputId) {
    ExternalElement externalElement = externalElementInfo.getElement();
    assertEquals(relativeUnitRow, externalElement.getRelativeUnitRow());
    assertEquals(relativeUnitColumn, externalElement.getRelativeUnitColumn());
    assertEquals(elementId, externalElement.getElementId());
    assertEquals(outputId, externalElement.getOutputId());
  }
}
