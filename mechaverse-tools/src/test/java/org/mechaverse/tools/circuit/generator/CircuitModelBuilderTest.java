package org.mechaverse.tools.circuit.generator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.tools.circuit.CircuitTestUtil;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.ElementInfo;
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
  }
}
