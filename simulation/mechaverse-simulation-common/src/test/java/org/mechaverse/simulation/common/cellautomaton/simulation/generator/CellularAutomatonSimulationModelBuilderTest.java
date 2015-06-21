package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.ROUTING_3IN3OUT_TYPE;

import org.junit.Test;
import org.mechaverse.cellautomaton.model.CellType;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.cellautomaton.model.Var;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.Routing3In3OutCellType;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonTestUtil.CellInfoVerifier;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonTestUtil.ExternalCellInfoVerifier;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.CellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.LogicalUnitInfo;

/**
 * Unit test {@link CellularAutomatonSimulationModelBuilder}.
 */
public class CellularAutomatonSimulationModelBuilderTest {

  /**
   * A cell that uses a {@link Var}.
   */
  public static class VarCellType extends CellType {

    private static final long serialVersionUID = 1L;

    public VarCellType() {
      setId("var");
      Var testVar = new Var();
      testVar.setId("testVar");
      getVars().add(testVar);
      getOutputs().add(CellularAutomatonBuilder.newOutput("1", "({input2} & {testVar})"));
    }

    public static VarCellType newInstance() {
      return new VarCellType();
    }
  }


  // TODO(thorntonv): Test cell type output duplication and restriction.
  // TODO(thorntonv): Test logical units with cells with fewer than 3 cells on the boundary.

  /**
   * Tests building the model of an automaton with a single logical unit that consists of a cell
   * with a {@link Var}.
   */
  @Test
  public void testBuildModel_cellWithVar() {
    CellularAutomatonDescriptor descriptor = CellularAutomatonBuilder.newCellularAutomaton(
      1, 1, VarCellType.newInstance(), 1, 1);
    descriptor.getLogicalUnit().setNeighborConnections("3");

    CellularAutomatonSimulationModelBuilder modelBuilder =
        new CellularAutomatonSimulationModelBuilder();
    CellularAutomatonSimulationModel model = modelBuilder.buildModel(descriptor);
    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();

    assertEquals(1, logicalUnitInfo.getCells().size());
    CellInfo cell = logicalUnitInfo.getCells().get(0);
    assertEquals(1, cell.getVarNames().size());
    assertNotNull(cell.getVarName("testVar"));
  }

  /**
   * Tests building the model of an automaton with a single logical unit that consists of two
   * routing3in3out cells in a single row.
   */
  @Test
  public void testBuildModel_singleRowAutomaton() {
    CellularAutomatonDescriptor descriptor1 = CellularAutomatonBuilder.newCellularAutomaton(
        1, 1, Routing3In3OutCellType.newInstance(), 1, 2);
    descriptor1.getLogicalUnit().setNeighborConnections("3");

    CellularAutomatonSimulationModelBuilder modelBuilder =
        new CellularAutomatonSimulationModelBuilder();
    CellularAutomatonSimulationModel model = modelBuilder.buildModel(descriptor1);
    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();

    // Verify logical unit.
    assertEquals(2, logicalUnitInfo.getCells().size());
    assertEquals(4, logicalUnitInfo.getExternalCells().size());
    for (CellInfo cell : logicalUnitInfo.getCells()) {
      assertEquals(3, cell.getOutputVarNames().size());
      assertEquals(6, cell.getParamVarNames().size());
      assertEquals(0, cell.getVarNames().size());
    }

    // 9 values per cell * 2 cells.
    assertEquals(9 * 2, logicalUnitInfo.getStateSize());

    // Verify external cells.
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in1"))
        .verifyRelativeRow(0).verifyRelativeColumn(-1)
        .verifyCellId("2").verifyOutputId("3");
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in2"))
        .verifyRelativeRow(-1).verifyRelativeColumn(0)
        .verifyCellId("1").verifyOutputId("2");
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in3"))
        .verifyRelativeRow(1).verifyRelativeColumn(0)
        .verifyCellId("2").verifyOutputId("2");
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in4"))
        .verifyRelativeRow(0).verifyRelativeColumn(1)
        .verifyCellId("1").verifyOutputId("1");

    // Verify cells.
    CellInfo cell1 = logicalUnitInfo.getCellInfo("1");
    CellInfo cell2 = logicalUnitInfo.getCellInfo("2");
    CellInfoVerifier.of(cell1).verifyId("1").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyExternalInput(0, "in1")
        .verifyExternalInput(1, "in2")
        .verifyInput(2, cell2, cell2.getOutputs().get(0))
        .verifyOutputVarNames("cell_1_out1", "cell_1_out2", "cell_1_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("cell_1"));
    CellInfoVerifier.of(cell2).verifyId("2").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, cell1, cell1.getOutputs().get(2))
        .verifyExternalInput(1, "in3")
        .verifyExternalInput(2, "in4")
        .verifyOutputVarNames("cell_2_out1", "cell_2_out2", "cell_2_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("cell_2"));
  }

  /**
   * Tests building the model of an automaton with 3x3 logical units that consists of a 3x3 matrix
   * of routing3in3out cells.
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
  public void testBuildModel_multiRowAutomaton() {
    CellularAutomatonDescriptor descriptor1 = CellularAutomatonBuilder.newCellularAutomaton(
        3, 3, Routing3In3OutCellType.newInstance(), 3, 3);
    descriptor1.getLogicalUnit().setNeighborConnections("3");

    CellularAutomatonSimulationModelBuilder modelBuilder =
        new CellularAutomatonSimulationModelBuilder();
    CellularAutomatonSimulationModel model = modelBuilder.buildModel(descriptor1);
    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();

    // Verify logical unit.
    assertEquals(9, logicalUnitInfo.getCells().size());
    assertEquals(9, logicalUnitInfo.getExternalCells().size());

    for (CellInfo cell : logicalUnitInfo.getCells()) {
      assertEquals(3, cell.getOutputVarNames().size());
      assertEquals(6, cell.getParamVarNames().size());
      assertEquals(0, cell.getVarNames().size());
    }

    // 9 values per cell * 9 cells.
    assertEquals(81, logicalUnitInfo.getStateSize());

    // Verify external cells.
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in1"))
        .verifyRelativeRow(0).verifyRelativeColumn(-1)
        .verifyCellId("3").verifyOutputId("3");
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in2"))
        .verifyRelativeRow(-1).verifyRelativeColumn(0)
        .verifyCellId("7").verifyOutputId("2");
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in3"))
        .verifyRelativeRow(-1).verifyRelativeColumn(0)
        .verifyCellId("9").verifyOutputId("2");
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in4"))
        .verifyRelativeRow(0).verifyRelativeColumn(1)
        .verifyCellId("1").verifyOutputId("1");
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in5"))
        .verifyRelativeRow(0).verifyRelativeColumn(-1)
        .verifyCellId("6").verifyOutputId("3");
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in6"))
        .verifyRelativeRow(0).verifyRelativeColumn(1)
        .verifyCellId("4").verifyOutputId("1");
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in7"))
        .verifyRelativeRow(0).verifyRelativeColumn(-1)
        .verifyCellId("9").verifyOutputId("3");
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in8"))
        .verifyRelativeRow(1).verifyRelativeColumn(0)
        .verifyCellId("2").verifyOutputId("2");
    ExternalCellInfoVerifier.of(logicalUnitInfo.getExternalCellInfo("in9"))
        .verifyRelativeRow(0).verifyRelativeColumn(1)
        .verifyCellId("7").verifyOutputId("1");

    // Verify cells.
    CellInfo cell1 = logicalUnitInfo.getCellInfo("1");
    CellInfo cell2 = logicalUnitInfo.getCellInfo("2");
    CellInfo cell3 = logicalUnitInfo.getCellInfo("3");
    CellInfo cell4 = logicalUnitInfo.getCellInfo("4");
    CellInfo cell5 = logicalUnitInfo.getCellInfo("5");
    CellInfo cell6 = logicalUnitInfo.getCellInfo("6");
    CellInfo cell7 = logicalUnitInfo.getCellInfo("7");
    CellInfo cell8 = logicalUnitInfo.getCellInfo("8");
    CellInfo cell9 = logicalUnitInfo.getCellInfo("9");

    CellInfoVerifier.of(cell1).verifyId("1").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyExternalInput(0, "in1")
        .verifyExternalInput(1, "in2")
        .verifyInput(2, cell2, cell2.getOutputs().get(0))
        .verifyOutputVarNames("cell_1_out1", "cell_1_out2", "cell_1_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("cell_1"));
    CellInfoVerifier.of(cell2).verifyId("2").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, cell1, cell1.getOutputs().get(2))
        .verifyInput(1, cell5, cell5.getOutputs().get(1))
        .verifyInput(2, cell3, cell3.getOutputs().get(0))
        .verifyOutputVarNames("cell_2_out1", "cell_2_out2", "cell_2_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("cell_2"));
    CellInfoVerifier.of(cell3).verifyId("3").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, cell2, cell2.getOutputs().get(2))
        .verifyExternalInput(1, "in3")
        .verifyExternalInput(2, "in4")
        .verifyOutputVarNames("cell_3_out1", "cell_3_out2", "cell_3_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("cell_3"));
    CellInfoVerifier.of(cell4).verifyId("4").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyExternalInput(0, "in5")
        .verifyInput(1, cell7, cell2.getOutputs().get(1))
        .verifyInput(2, cell5, cell2.getOutputs().get(0))
        .verifyOutputVarNames("cell_4_out1", "cell_4_out2", "cell_4_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("cell_4"));
    CellInfoVerifier.of(cell5).verifyId("5").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, cell4, cell4.getOutputs().get(2))
        .verifyInput(1, cell2, cell2.getOutputs().get(1))
        .verifyInput(2, cell6, cell6.getOutputs().get(0))
        .verifyOutputVarNames("cell_5_out1", "cell_5_out2", "cell_5_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("cell_5"));
    CellInfoVerifier.of(cell6).verifyId("6").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, cell5, cell5.getOutputs().get(2))
        .verifyInput(1, cell9, cell9.getOutputs().get(1))
        .verifyExternalInput(2, "in6")
        .verifyOutputVarNames("cell_6_out1", "cell_6_out2", "cell_6_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("cell_6"));
    CellInfoVerifier.of(cell7).verifyId("7").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyExternalInput(0, "in7")
        .verifyInput(1, cell4, cell4.getOutputs().get(1))
        .verifyInput(2, cell8, cell8.getOutputs().get(0))
        .verifyOutputVarNames("cell_7_out1", "cell_7_out2", "cell_7_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("cell_7"));
    CellInfoVerifier.of(cell8).verifyId("8").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, cell7, cell7.getOutputs().get(2))
        .verifyExternalInput(1, "in8")
        .verifyInput(2, cell9, cell9.getOutputs().get(0))
        .verifyOutputVarNames("cell_8_out1", "cell_8_out2", "cell_8_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("cell_8"));
    CellInfoVerifier.of(cell9).verifyId("9").verifyType(ROUTING_3IN3OUT_TYPE)
        .verifyInputCount(3)
        .verifyInput(0, cell8, cell8.getOutputs().get(2))
        .verifyInput(1, cell6, cell6.getOutputs().get(1))
        .verifyExternalInput(2, "in9")
        .verifyOutputVarNames("cell_9_out1", "cell_9_out2", "cell_9_out3")
        .verifyOutputParamVarNames(getExpectedOutputParamNames("cell_9"));
  }

  private String[][][] getExpectedOutputParamNames(String elem) {
    return new String[][][]{
        {{"input2Mask", elem + "_out1_input2Mask"}, {"input3Mask", elem + "_out1_input3Mask"}},
        {{"input1Mask", elem + "_out2_input1Mask"}, {"input3Mask", elem + "_out2_input3Mask"}},
        {{"input1Mask", elem + "_out3_input1Mask"}, {"input2Mask", elem + "_out3_input2Mask"}}};
  }
}
