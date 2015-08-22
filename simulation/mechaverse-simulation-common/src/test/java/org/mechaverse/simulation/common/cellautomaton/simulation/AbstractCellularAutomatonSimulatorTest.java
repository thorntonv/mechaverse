package org.mechaverse.simulation.common.cellautomaton.simulation;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.CellularAutomatonStateBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.InputCellType;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.LogicalUnitStateBuilder;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.Routing3In3OutCellType;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonBuilder.ToggleCellType;

import com.google.common.collect.ImmutableList;
/**
 * Abstract base test for {@link CellularAutomatonSimulator} implementations.
 */
public abstract class AbstractCellularAutomatonSimulatorTest {

  // A simple routing automaton with 3x3 logical units. Each logical unit has 4x4 3-in/out routing
  // cells.
  //
  //        [2]       [3]
  // [1] ----1----2----3----4--- [4]
  //              |         |
  // [5] ----5----6----7----8--- [6]
  //         |         |
  // [7] ----9---10---11---12--- [8]
  //              |         |
  // [9] ---13---14---15---16--- [12]
  //       [10]      [11]
  //
  private CellularAutomatonDescriptor routingAutomaton;

  protected abstract CellularAutomatonSimulator newSimulator(
      CellularAutomatonDescriptor descriptor, int automatonCount) throws Exception;

  @Before
  public void setUp() {
    routingAutomaton = CellularAutomatonBuilder.newCellularAutomaton(
        3, 3, Routing3In3OutCellType.newInstance(), 4, 4);
    routingAutomaton.getLogicalUnit().setNeighborConnections("3");
  }

  @Test
  public void getAndSetState() throws Exception {
    CellularAutomatonSimulator simulator = newSimulator(routingAutomaton, 1);

    int[] expectedState = new int[simulator.getAutomatonStateSize()];
    CellularAutomatonTestUtil.setRandomState(expectedState);
    simulator.setAutomatonState(0, expectedState);
    int[] actualState = new int[simulator.getAutomatonStateSize()];
    simulator.getAutomatonState(0, actualState);

    assertArrayEquals(expectedState, actualState);
  }

  @Test
  public void routingAutomaton_stateSize() throws Exception {
    CellularAutomatonSimulator simulator = newSimulator(routingAutomaton, 1);

    int wordsPerCell = 3 + 2 * 3; // 3 outputs + 2 params per output.
    int cellsPerLu = 4 * 4;
    int luPerAutomaton = 3 * 3;
    assertEquals(wordsPerCell * cellsPerLu * luPerAutomaton, simulator.getAutomatonStateSize());
  }

  @Test
  public void multipleAutomaton_stateSize() throws Exception {
    CellularAutomatonSimulator simulator = newSimulator(routingAutomaton, 5);

    int wordsPerCell = 3 + 2 * 3; // 3 outputs + 2 params per output.
    int cellsPerLu = 4 * 4;
    int luPerAutomaton = 3 * 3;
    assertEquals(wordsPerCell * cellsPerLu * luPerAutomaton, simulator.getAutomatonStateSize());
  }

  @Test
  public void update_internalRoutingCell() throws Exception {
    CellularAutomatonStateBuilder stateBuilder =
        CellularAutomatonStateBuilder.of(routingAutomaton, 1);
    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 0);

    // cell_10_out1 = cell_14_out2 or cell_11_out1.
    // cell_10_out2 = cell_9_out3 or cell_11_out1.
    // cell_10_out3 = cell_9_out3 or cell_14_out2.
    // cell_9 is updated before cell_10 and has value 0.
    luStateBuilder.set("cell_14_out2", 0b010);
    luStateBuilder.set("cell_11_out1", 0b100);
    luStateBuilder.set("cell_10_out1_input2Mask", 0b111);
    luStateBuilder.set("cell_10_out1_input3Mask", 0b110);
    luStateBuilder.set("cell_10_out2_input1Mask", 0b111);
    luStateBuilder.set("cell_10_out2_input3Mask", 0b111);
    luStateBuilder.set("cell_10_out3_input1Mask", 0b111);
    luStateBuilder.set("cell_10_out3_input2Mask", 0b111);

    CellularAutomatonSimulator simulator = newSimulator(routingAutomaton, 1);
    simulator.setAutomatonState(0, stateBuilder.getState());
    simulator.update();
    simulator.getAutomatonState(0, stateBuilder.getState());

    assertEquals(0b110, luStateBuilder.get("cell_10_out1"));
    assertEquals(0b100, luStateBuilder.get("cell_10_out2"));
    assertEquals(0b010, luStateBuilder.get("cell_10_out3"));
  }

  @Test
  public void update_routingAutomaton_luUpperRightBoundaryCell() throws Exception {
    CellularAutomatonStateBuilder stateBuilder =
        CellularAutomatonStateBuilder.of(routingAutomaton, 1);
    stateBuilder.setAll(0b111);

    // cell_4_out2 = cell_3_out3 or cell_1_out1 (lu to the right).
    stateBuilder.luStateBuilder(0, 5).set("cell_1_out1", 0b101);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 4);
    luStateBuilder.set("cell_3_out3", 0b010);
    luStateBuilder.set("cell_4_out2_input1Mask", 0b000);
    luStateBuilder.set("cell_4_out2_input3Mask", 0b110);

    CellularAutomatonSimulator simulator = newSimulator(routingAutomaton, 1);
    simulator.setAutomatonState(0, stateBuilder.getState());
    simulator.update();
    simulator.getAutomatonState(0, stateBuilder.getState());

    assertEquals(0b100, luStateBuilder.get("cell_4_out2"));
  }

  @Test
  public void update_routingAutomaton_luLowerRightBoundaryCell() throws Exception {
    CellularAutomatonStateBuilder stateBuilder =
        CellularAutomatonStateBuilder.of(routingAutomaton, 1);
    stateBuilder.setAll(0b111);

    // cell_16_out1 = cell_12_out2 or cell_13_out1 (lu to the right).
    stateBuilder.luStateBuilder(0, 5).set("cell_13_out1", 0b101);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 4);
    luStateBuilder.set("cell_12_out2", 0b010);
    luStateBuilder.set("cell_16_out1_input2Mask", 0b001);
    luStateBuilder.set("cell_16_out1_input3Mask", 0b110);

    CellularAutomatonSimulator simulator = newSimulator(routingAutomaton, 1);
    simulator.setAutomatonState(0, stateBuilder.getState());
    simulator.update();
    simulator.getAutomatonState(0, stateBuilder.getState());

    assertEquals(0b101, luStateBuilder.get("cell_16_out1"));
  }

  @Test
  public void update_routingAutomaton_upperLeftBoundaryCell() throws Exception {
    CellularAutomatonStateBuilder stateBuilder =
        CellularAutomatonStateBuilder.of(routingAutomaton, 1);
    stateBuilder.setAll(0b111);

    // cell_1_out2 = cell_4_out3 (lu to the left) or cell_2_out1.
    stateBuilder.luStateBuilder(0, 2).set("cell_4_out3", 0b101);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 0);
    luStateBuilder.set("cell_2_out1", 0b010);
    luStateBuilder.set("cell_1_out2_input1Mask", 0b111);
    luStateBuilder.set("cell_1_out2_input3Mask", 0b000);

    CellularAutomatonSimulator simulator = newSimulator(routingAutomaton, 1);
    simulator.setAutomatonState(0, stateBuilder.getState());
    simulator.update();
    simulator.getAutomatonState(0, stateBuilder.getState());

    assertEquals(0b101, luStateBuilder.get("cell_1_out2"));
  }

  @Test
  public void update_routingAutomaton_leftBoundaryCell() throws Exception {
    CellularAutomatonStateBuilder stateBuilder =
        CellularAutomatonStateBuilder.of(routingAutomaton, 1);
    stateBuilder.setAll(0b111);

    // cell_1_out2 = cell_4_out3 (lu to the left) or cell_2_out1.
    stateBuilder.luStateBuilder(0, 5).set("cell_4_out3", 0b101);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 3);
    luStateBuilder.set("cell_2_out1", 0b010);
    luStateBuilder.set("cell_1_out2_input1Mask", 0b111);
    luStateBuilder.set("cell_1_out2_input3Mask", 0b000);

    CellularAutomatonSimulator simulator = newSimulator(routingAutomaton, 1);
    simulator.setAutomatonState(0, stateBuilder.getState());
    simulator.update();
    simulator.getAutomatonState(0, stateBuilder.getState());

    assertEquals(0b101, luStateBuilder.get("cell_1_out2"));
  }

  @Test
  public void inputCell() throws Exception {
    String[][] cellTypeIds =
        {{CellularAutomatonBuilder.INPUT_TYPE, CellularAutomatonBuilder.ROUTING_3IN3OUT_TYPE}};
    CellularAutomatonDescriptor descriptor = CellularAutomatonBuilder.newCellularAutomaton(
        3, 3, ImmutableList.of(InputCellType.newInstance(), Routing3In3OutCellType.newInstance()), cellTypeIds);
    CellularAutomatonSimulator simulator = newSimulator(descriptor, 1);

    int[] input = new int[simulator.getAutomatonInputSize()];
    assertTrue(input.length > 1);
    input[1] = 0b101;
    simulator.setAutomatonInput(0, input);

    CellularAutomatonStateBuilder stateBuilder = CellularAutomatonStateBuilder.of(descriptor, 1);
    stateBuilder.setAll(0b111);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 1);
    luStateBuilder.set("cell_1_out1_idx", 1);
    luStateBuilder.set("cell_2_out3", 0b111);
    luStateBuilder.set("cell_2_out3_input1Mask", 0b111);
    luStateBuilder.set("cell_2_out3_input2Mask", 0b000);

    simulator.setAutomatonState(0, stateBuilder.getState());
    simulator.update();

    simulator.getAutomatonState(0, stateBuilder.getState());

    assertEquals(0b101, luStateBuilder.get("cell_2_out3"));
  }

  @Test
  public void outputMap() throws Exception {
    CellularAutomatonStateBuilder stateBuilder =
        CellularAutomatonStateBuilder.of(routingAutomaton, 1);
    stateBuilder.setAll(0b111);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 0);
    luStateBuilder.set("cell_3_out1", 0b010);
    luStateBuilder.set("cell_2_out1_input2Mask", 0b000);
    luStateBuilder.set("cell_2_out1_input3Mask", 0b111);

    CellularAutomatonSimulator simulator = newSimulator(routingAutomaton, 1);
    simulator.setAutomatonState(0, stateBuilder.getState());
    int[] outputMap = new int[simulator.getAutomatonOutputSize()];
    outputMap[0] = luStateBuilder.getStateIndex("cell_2_out1");
    simulator.setAutomatonOutputMap(0, outputMap);
    simulator.update();
    simulator.getAutomatonState(0, stateBuilder.getState());
    int[] output = new int[simulator.getAutomatonOutputSize()];
    simulator.getAutomatonOutput(0, output);
    assertEquals(0b010, luStateBuilder.get("cell_2_out1"));
    assertEquals(0b010, output[0]);
  }

  @Test
  public void memoryCell() throws Exception {
    CellularAutomatonDescriptor descriptor =
        CellularAutomatonBuilder.newCellularAutomaton(3, 3, ToggleCellType.newInstance(), 3, 3);
    CellularAutomatonSimulator simulator = newSimulator(descriptor, 1);

    CellularAutomatonStateBuilder stateBuilder = CellularAutomatonStateBuilder.of(descriptor, 1);
    stateBuilder.setAll(0b111);

    LogicalUnitStateBuilder luStateBuilder = stateBuilder.luStateBuilder(0, 0);
    luStateBuilder.set("cell_2_out1", 0b001);
    luStateBuilder.set("cell_2_out2", 0b101);
    luStateBuilder.set("cell_2_out3", 0b100);

    simulator.setAutomatonState(0, stateBuilder.getState());
    simulator.update();

    simulator.getAutomatonState(0, stateBuilder.getState());
    assertEquals(0b110, luStateBuilder.get("cell_2_out1") & 0b111);
    assertEquals(0b010, luStateBuilder.get("cell_2_out2") & 0b111);
    assertEquals(0b011, luStateBuilder.get("cell_2_out3") & 0b111);

    simulator.update();
    simulator.getAutomatonState(0, stateBuilder.getState());
    assertEquals(0b001, luStateBuilder.get("cell_2_out1"));
    assertEquals(0b101, luStateBuilder.get("cell_2_out2"));
    assertEquals(0b100, luStateBuilder.get("cell_2_out3"));
  }

  @Test
  public void update_multipleAutomata() throws Exception {
    int numAutomata = 50;
    CellularAutomatonSimulator simulator = newSimulator(routingAutomaton, numAutomata);

    int[] state = new int[simulator.getAutomatonStateSize()];
    CellularAutomatonTestUtil.setRandomState(state);
    for(int idx = 0; idx < numAutomata; idx++) {
      simulator.setAutomatonState(idx, state);
    }
    simulator.update();
    int[] expectedState = new int[simulator.getAutomatonStateSize()];
    simulator.getAutomatonState(0, expectedState);
    for(int idx = 1; idx < numAutomata; idx++) {
      simulator.getAutomatonState(idx, state);
      assertArrayEquals(expectedState, state);
    }
  }

  /**
   * Tests that state after a single update with N iterations is equivalent to N updates with a
   * single iteration.
   */
  @Test
  public void update_multipleIterations() throws Exception {
    int iterationsPerUpdate = 200;
    CellularAutomatonDescriptor descriptor1 = CellularAutomatonBuilder.newCellularAutomaton(
      3, 3, Routing3In3OutCellType.newInstance(), 4, 4);
    CellularAutomatonDescriptor descriptor2 = CellularAutomatonBuilder.newCellularAutomaton(
      3, 3, Routing3In3OutCellType.newInstance(), 4, 4);
    descriptor2.setIterationsPerUpdate(iterationsPerUpdate);

    CellularAutomatonSimulator simulator1 = newSimulator(descriptor1, 1);
    CellularAutomatonSimulator simulator2 = newSimulator(descriptor2, 1);

    int[] state1 = new int[simulator1.getAutomatonStateSize()];
    int[] state2 = new int[simulator1.getAutomatonStateSize()];
    CellularAutomatonTestUtil.setRandomState(state1);
    simulator1.setAutomatonState(0, state1);
    simulator2.setAutomatonState(0, state1);

    for(int cnt = 0; cnt < iterationsPerUpdate; cnt++) {
      simulator1.update();
    }

    simulator2.update();

    simulator1.getAutomatonState(0, state1);
    simulator2.getAutomatonState(0, state2);

    assertArrayEquals(state1, state2);
  }

  @Test
  public void automatonAllocationAndDeallocation() throws Exception {
    CellularAutomatonDescriptor descriptor1 = CellularAutomatonBuilder.newCellularAutomaton(
        3, 3, Routing3In3OutCellType.newInstance(), 4, 4);
    CellularAutomatonSimulator simulator = newSimulator(descriptor1, 10);
    assertEquals(10, simulator.getAllocator().getAvailableCount());

    Set<Integer> allocatedAutomata = new HashSet<>();
    for (int cnt = 1; cnt <= 10; cnt++) {
      allocatedAutomata.add(simulator.getAllocator().allocate());
    }
    assertEquals(10, allocatedAutomata.size());

    try {
      simulator.getAllocator().allocate();
      fail("Expected exception was not thrown.");
    } catch (IllegalStateException ex) {
      // Expected.
    }

    simulator.getAllocator().deallocate(4);
    assertEquals(4, simulator.getAllocator().allocate());
  }
}
