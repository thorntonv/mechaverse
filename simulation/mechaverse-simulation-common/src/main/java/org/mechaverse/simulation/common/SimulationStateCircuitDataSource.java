package org.mechaverse.simulation.common;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitDataSource;
import org.mechaverse.simulation.common.circuit.CircuitReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link CircuitDataSource} implementation that loads a {@link Circuit} from the simulation
 * state.
 */
public class SimulationStateCircuitDataSource implements CircuitDataSource {

  public static String CIRCUIT_XML_KEY = "circuit-xml";

  private static final Logger logger = LoggerFactory.getLogger(SimulationStateCircuitDataSource.class);

  @Autowired private Simulation simulation;

  @Override
  public Circuit getCircuit() {
    try {
      if (simulation.getState().containsKey(CIRCUIT_XML_KEY)) {
        return CircuitReader.read(simulation.getState().get(CIRCUIT_XML_KEY));
      }
    } catch (Throwable e) {}

    // TODO(thorntonv): Remove fallback to circuit xml on the classpath.
    try {
      return CircuitReader.read(ClassLoader.getSystemResourceAsStream("circuit.xml"));
    } catch (Throwable t) {
      logger.warn("Unable to load circuit xml.", t);
      throw new IllegalStateException("Unable to load circuit xml.", t);
    }
  }
}
