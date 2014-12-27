package org.mechaverse.simulation.common.cellautomaton.examples;

import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulationUtil;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonVisualizer;

public class OpenClBooleanFunction3 {

  public static void main(String[] args) throws Exception {
    CellularAutomatonDescriptor descriptor = CellularAutomatonDescriptorReader.read(
        ClassLoader.getSystemResourceAsStream("boolean3.xml"));
    CellularAutomatonSimulator simulator =
        new OpenClCellularAutomatonSimulator(1, 1, 1, descriptor);
    SimulatorCellularAutomaton cells = new SimulatorCellularAutomaton(descriptor, simulator);
    CellularAutomatonVisualizer visualizer =
        new CellularAutomatonVisualizer(cells, BooleanFunction3.CELL_COLOR_PROVIDER);
    int[] state = CellularAutomatonSimulationUtil.randomState(
        simulator.getAutomatonStateSize(), new Well19937c());
    cells.setState(state);
    visualizer.start();
  }
}
