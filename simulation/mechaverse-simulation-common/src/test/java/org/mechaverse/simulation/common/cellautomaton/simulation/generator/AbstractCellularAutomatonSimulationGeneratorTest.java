package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.Routing3In3OutCellType;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.CellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.LogicalUnitInfo;

public class AbstractCellularAutomatonSimulationGeneratorTest {

  private static class TestSimulationGenerator 
      extends AbstractCellularAutomatonSimulationGenerator {

    public TestSimulationGenerator(CellularAutomatonSimulationModel model) {
      super(model);
    }

    @Override
    public void generate(PrintWriter out) {}
  }

  protected CellularAutomatonDescriptor descriptor1;

  @Before
  public void setUp() {
    descriptor1 = CellularAutomatonBuilder.newCellularAutomaton(
        1, 1, Routing3In3OutCellType.newInstance(), 1, 2);
    descriptor1.getLogicalUnit().setNeighborConnections("3");
  }

  @Test
  public void testGetVarMappedExpression() {
    CellularAutomatonSimulationModelBuilder modelBuilder =
        new CellularAutomatonSimulationModelBuilder();
    CellularAutomatonSimulationModel model = modelBuilder.buildModel(descriptor1);
    TestSimulationGenerator generator = new TestSimulationGenerator(model);

    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();
    CellInfo cell1 = logicalUnitInfo.getCellInfo("1");
    CellInfo cell2 = logicalUnitInfo.getCellInfo("2");

    // Test cell 1 output expressions.
    assertEquals("(ex_in2 & cell_1_out1_input2Mask) | (cell_2_out1 & cell_1_out1_input3Mask)",
        getVarMappedUpdateExpression(cell1, 0, generator));
    assertEquals("(ex_in1 & cell_1_out2_input1Mask) | (cell_2_out1 & cell_1_out2_input3Mask)",
        getVarMappedUpdateExpression(cell1, 1, generator));
    assertEquals("(ex_in1 & cell_1_out3_input1Mask) | (ex_in2 & cell_1_out3_input2Mask)",
        getVarMappedUpdateExpression(cell1, 2, generator));

    // Test cell 2 output expressions.
    assertEquals("(ex_in3 & cell_2_out1_input2Mask) | (ex_in4 & cell_2_out1_input3Mask)",
        getVarMappedUpdateExpression(cell2, 0, generator));
    assertEquals("(cell_1_out3 & cell_2_out2_input1Mask) | (ex_in4 & cell_2_out2_input3Mask)",
        getVarMappedUpdateExpression(cell2, 1, generator));
    assertEquals("(cell_1_out3 & cell_2_out3_input1Mask) | (ex_in3 & cell_2_out3_input2Mask)",
        getVarMappedUpdateExpression(cell2, 2, generator));
  }
  
  private static String getVarMappedUpdateExpression(
      CellInfo cell, int outputIdx, AbstractCellularAutomatonSimulationGenerator generator) {
    return generator.getVarMappedString(cell.getOutputs().get(outputIdx).getUpdateExpression(),
        cell, cell.getOutputs().get(outputIdx));
  }
}
