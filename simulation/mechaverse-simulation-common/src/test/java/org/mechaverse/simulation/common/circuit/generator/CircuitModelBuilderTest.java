package org.mechaverse.simulation.common.circuit.generator;

import static org.junit.Assert.assertEquals;
import static org.mechaverse.simulation.common.circuit.CircuitBuilder.ROUTING_3IN3OUT_TYPE;

import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitBuilder;
import org.mechaverse.simulation.common.circuit.CircuitBuilder.Routing3In3OutElementType;
import org.mechaverse.simulation.common.circuit.CircuitTestUtil.ElementInfoVerifier;
import org.mechaverse.simulation.common.circuit.CircuitTestUtil.ExternalElementInfoVerifier;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;

/**
 * Unit test {@link CircuitSimulationModelBuilder}.
 *
 * @author thorntonv@mechaverse.org
 */
public class CircuitModelBuilderTest {

  // TODO(thorntonv): Test element type output duplication and restriction.
  // TODO(thorntonv): Test logical units with elements with fewer than 3 elements on the boundary.

  /**
   * Tests building the model of a circuit with a single logical unit that consists of two
   * routing3in3out elements in a single row.
   */
  @Test
  public void testBuildModel_singleRowCircuit() {
    Circuit circuit1 =
        CircuitBuilder.newCircuit(1, 1, Routing3In3OutElementType.newInstance(), 1, 2);

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
    ElementInfoVerifier.of(element1).verifyId("1").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyExternalInput(0, "in1")
        .verifyExternalInput(1, "in2")
        .verifyInput(2, element2, element2.getOutputs().get(0))
        .verifyOutputVarNames("e1_out1", "e1_out2", "e1_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("e1"));
    ElementInfoVerifier.of(element2).verifyId("2").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, element1, element1.getOutputs().get(2))
        .verifyExternalInput(1, "in3")
        .verifyExternalInput(2, "in4")
        .verifyOutputVarNames("e2_out1", "e2_out2", "e2_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("e2"));
  }

  /**
   * Tests building the model of a circuit with 3x3 logical units that consists of a 3x3 matrix of
   * routing3in3out elements.
   *
   *      [2]     [3]
   * [1] --1---2---3-- [4]
   *           |
   * [5] --4---5---6-- [6]
   *       |       |
   * [7] --7---8---9-- [9]
   *          [8]
   */
  @Test
  public void testBuildModel_multiRowCircuit() {
    Circuit circuit1 =
        CircuitBuilder.newCircuit(3, 3, Routing3In3OutElementType.newInstance(), 3, 3);

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

    // Verify elements.
    ElementInfo element1 = logicalUnitInfo.getElementInfo("1");
    ElementInfo element2 = logicalUnitInfo.getElementInfo("2");
    ElementInfo element3 = logicalUnitInfo.getElementInfo("3");
    ElementInfo element4 = logicalUnitInfo.getElementInfo("4");
    ElementInfo element5 = logicalUnitInfo.getElementInfo("5");
    ElementInfo element6 = logicalUnitInfo.getElementInfo("6");
    ElementInfo element7 = logicalUnitInfo.getElementInfo("7");
    ElementInfo element8 = logicalUnitInfo.getElementInfo("8");
    ElementInfo element9 = logicalUnitInfo.getElementInfo("9");

    ElementInfoVerifier.of(element1).verifyId("1").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyExternalInput(0, "in1")
        .verifyExternalInput(1, "in2")
        .verifyInput(2, element2, element2.getOutputs().get(0))
        .verifyOutputVarNames("e1_out1", "e1_out2", "e1_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("e1"));
    ElementInfoVerifier.of(element2).verifyId("2").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, element1, element1.getOutputs().get(2))
        .verifyInput(1, element5, element5.getOutputs().get(1))
        .verifyInput(2, element3, element3.getOutputs().get(0))
        .verifyOutputVarNames("e2_out1", "e2_out2", "e2_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("e2"));
    ElementInfoVerifier.of(element3).verifyId("3").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, element2, element2.getOutputs().get(2))
        .verifyExternalInput(1, "in3")
        .verifyExternalInput(2, "in4")
        .verifyOutputVarNames("e3_out1", "e3_out2", "e3_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("e3"));
    ElementInfoVerifier.of(element4).verifyId("4").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyExternalInput(0, "in5")
        .verifyInput(1, element7, element2.getOutputs().get(1))
        .verifyInput(2, element5, element2.getOutputs().get(0))
        .verifyOutputVarNames("e4_out1", "e4_out2", "e4_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("e4"));
    ElementInfoVerifier.of(element5).verifyId("5").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, element4, element4.getOutputs().get(2))
        .verifyInput(1, element2, element2.getOutputs().get(1))
        .verifyInput(2, element6, element6.getOutputs().get(0))
        .verifyOutputVarNames("e5_out1", "e5_out2", "e5_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("e5"));
    ElementInfoVerifier.of(element6).verifyId("6").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, element5, element5.getOutputs().get(2))
        .verifyInput(1, element9, element9.getOutputs().get(1))
        .verifyExternalInput(2, "in6")
        .verifyOutputVarNames("e6_out1", "e6_out2", "e6_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("e6"));
    ElementInfoVerifier.of(element7).verifyId("7").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyExternalInput(0, "in7")
        .verifyInput(1, element4, element4.getOutputs().get(1))
        .verifyInput(2, element8, element8.getOutputs().get(0))
        .verifyOutputVarNames("e7_out1", "e7_out2", "e7_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("e7"));
    ElementInfoVerifier.of(element8).verifyId("8").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, element7, element7.getOutputs().get(2))
        .verifyExternalInput(1, "in8")
        .verifyInput(2, element9, element9.getOutputs().get(0))
        .verifyOutputVarNames("e8_out1", "e8_out2", "e8_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("e8"));
    ElementInfoVerifier.of(element9).verifyId("9").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, element8, element8.getOutputs().get(2))
        .verifyInput(1, element6, element6.getOutputs().get(1))
        .verifyExternalInput(2, "in9")
        .verifyOutputVarNames("e9_out1", "e9_out2", "e9_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("e9"));
  }

  private String[][][] getExpectedOutputParamNames(String elem) {
    return new String[][][]{
        {{"input2Mask", elem + "_out1_input2Mask"}, {"input3Mask", elem + "_out1_input3Mask"}},
        {{"input1Mask", elem + "_out2_input1Mask"}, {"input3Mask", elem + "_out2_input3Mask"}},
        {{"input1Mask", elem + "_out3_input1Mask"}, {"input2Mask", elem + "_out3_input2Mask"}}};
  }
}
