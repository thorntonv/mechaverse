package org.mechaverse.tools.circuit.generator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.tools.circuit.CircuitTestUtil;
import org.mechaverse.tools.circuit.CircuitTestUtil.ElementInfoVerifier;
import org.mechaverse.tools.circuit.CircuitTestUtil.ExternalElementInfoVerifier;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;

/**
 * Unit test {@link CircuitSimulationModelBuilder}.
 *
 * @author thorntonv@mechaverse.org
 */
public class CircuitModelBuilderTest {

  /**
   * Tests building the model of a circuit with a single logical unit that consists of two
   * routing3in3out elements in a single row.
   */
  @Test
  public void testBuildModel_circuit1() {
    Circuit circuit1 = CircuitTestUtil.createTestCircuit(
        1, 1, CircuitTestUtil.createRouting3in3OutElementType(), 1, 2);

    CircuitSimulationModelBuilder modelBuilder = new CircuitSimulationModelBuilder();
    CircuitSimulationModel model = modelBuilder.buildModel(circuit1);
    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();

    // Verify logical unit.
    assertEquals(2, logicalUnitInfo.getElements().size());
    assertEquals(4, logicalUnitInfo.getExternalElements().size());
    // 9 values per element * 2 elements.
    assertEquals(9*2, logicalUnitInfo.getStateSize());

    // Verify external elements.
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in1"))
        .verifyRelativeRow(0).verifyRelativeColumn(-1)
        .verifyElementId("2").verifyOutputId("3");
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in2"))
        .verifyRelativeRow(-1).verifyRelativeColumn(0)
        .verifyElementId("1").verifyOutputId("2");
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in3"))
        .verifyRelativeRow(1).verifyRelativeColumn(0)
        .verifyElementId("2").verifyOutputId("2");
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in4"))
        .verifyRelativeRow(0).verifyRelativeColumn(1)
        .verifyElementId("1").verifyOutputId("1");

    // Verify elements.
    ElementInfo element1 = logicalUnitInfo.getElementInfo("1");
    ElementInfo element2 = logicalUnitInfo.getElementInfo("2");
    ElementInfoVerifier.of(element1).verifyId("1").verifyType(CircuitTestUtil.ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyExternalInput(0, "in1")
        .verifyExternalInput(1, "in2")
        .verifyInput(2, element2, element2.getOutputs().get(0))
        .verifyOutputVarNames("e1_out1", "e1_out2", "e1_out3")
        .verifyOutputParamVarNames(new String[][][]{
            {{"input2Mask", "e1_out1_input2Mask"}, {"input3Mask", "e1_out1_input3Mask"}},
            {{"input1Mask", "e1_out2_input1Mask"}, {"input3Mask", "e1_out2_input3Mask"}},
            {{"input1Mask", "e1_out3_input1Mask"}, {"input2Mask", "e1_out3_input2Mask"}}});
    ElementInfoVerifier.of(element2).verifyId("2").verifyType(CircuitTestUtil.ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, element1, element1.getOutputs().get(2))
        .verifyExternalInput(1, "in3")
        .verifyExternalInput(2, "in4")
        .verifyOutputVarNames("e2_out1", "e2_out2", "e2_out3")
        .verifyOutputParamVarNames(new String[][][]{
            {{"input2Mask", "e2_out1_input2Mask"}, {"input3Mask", "e2_out1_input3Mask"}},
            {{"input1Mask", "e2_out2_input1Mask"}, {"input3Mask", "e2_out2_input3Mask"}},
            {{"input1Mask", "e2_out3_input1Mask"}, {"input2Mask", "e2_out3_input2Mask"}}});
  }

  /**
   * Tests building the model of a circuit with 3x3 logical units that consists of a 3x3 matrix of
   * routing3in3out elements.
   *
   *    2           3
   * 1 [1]   [2]   [3] 4
   * 5 [4]   [5]   [6] 6
   * 7 [7]   [8]   [9] 9
   *          8
   */
  @Test
  public void testBuildModel_circuit2() {
    Circuit circuit1 = CircuitTestUtil.createTestCircuit(
        3, 3, CircuitTestUtil.createRouting3in3OutElementType(), 3, 3);

    CircuitSimulationModelBuilder modelBuilder = new CircuitSimulationModelBuilder();
    CircuitSimulationModel model = modelBuilder.buildModel(circuit1);
    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();

    // Verify logical unit.
    assertEquals(9, logicalUnitInfo.getElements().size());
    assertEquals(9, logicalUnitInfo.getExternalElements().size());
    // 9 values per element * 9 elements.
    assertEquals(81, logicalUnitInfo.getStateSize());

    // Verify external elements.
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in1"))
        .verifyRelativeRow(0).verifyRelativeColumn(-1)
        .verifyElementId("3").verifyOutputId("3");
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in2"))
        .verifyRelativeRow(-1).verifyRelativeColumn(0)
        .verifyElementId("7").verifyOutputId("2");
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in3"))
        .verifyRelativeRow(-1).verifyRelativeColumn(0)
        .verifyElementId("9").verifyOutputId("2");
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in4"))
        .verifyRelativeRow(0).verifyRelativeColumn(1)
        .verifyElementId("1").verifyOutputId("1");
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in5"))
        .verifyRelativeRow(0).verifyRelativeColumn(-1)
        .verifyElementId("6").verifyOutputId("3");
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in6"))
        .verifyRelativeRow(0).verifyRelativeColumn(1)
        .verifyElementId("4").verifyOutputId("1");
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in7"))
        .verifyRelativeRow(0).verifyRelativeColumn(-1)
        .verifyElementId("9").verifyOutputId("3");
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in8"))
        .verifyRelativeRow(1).verifyRelativeColumn(0)
        .verifyElementId("2").verifyOutputId("2");
    ExternalElementInfoVerifier.of(logicalUnitInfo.getExternalElementInfo("in9"))
        .verifyRelativeRow(0).verifyRelativeColumn(1)
        .verifyElementId("7").verifyOutputId("1");
  }
}
