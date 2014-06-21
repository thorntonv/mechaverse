package org.mechaverse.simulation.common.circuit.generator;

import java.io.PrintWriter;

/**
 * Generates source code for a circuit simulation.
 */
public interface CircuitSimulationGenerator {

  void generate(PrintWriter out);
}
