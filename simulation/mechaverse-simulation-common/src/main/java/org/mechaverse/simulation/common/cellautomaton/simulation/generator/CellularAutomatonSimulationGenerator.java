package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import java.io.PrintWriter;

/**
 * Generates source code for a cellular automaton simulation.
 */
public interface CellularAutomatonSimulationGenerator {

  void generate(PrintWriter out);
}
