package org.mechaverse.simulation.common;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitDataSource;
import org.mechaverse.simulation.common.circuit.CircuitReader;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link CircuitDataSource} implementation that loads a {@link Circuit} from the simulation
 * state.
 */
public class SimulationStateCircuitDataSource implements CircuitDataSource {

  public static String CIRCUIT_XML_KEY = "circuit-xml";

  private static final Logger logger =
      LoggerFactory.getLogger(SimulationStateCircuitDataSource.class);

  @Autowired private Simulation simulation;

  private Circuit circuit;
  private CircuitSimulationModel circuitModel;

  @Override
  public Circuit getCircuit() {
    if (circuit == null) {
      loadCircuit();
    }
    return circuit;
  }

  @Override
  public CircuitSimulationModel getCircuitSimulationModel() {
    if (circuitModel == null) {
      loadCircuit();
    }
    return circuitModel;
  }

  private void loadCircuit() {
    try {
      SimulationDataStore state = simulation.getState();
      if (state.containsKey(CIRCUIT_XML_KEY)) {
        circuit = CircuitReader.read(state.get(CIRCUIT_XML_KEY));
      }
    } catch (Throwable e) {
      logger.warn("Error reading circuit from state");
    }

    if (circuit == null) {
      // Attempt to load from circuit.xml on the classpath.
      try {
        circuit = CircuitReader.read(ClassLoader.getSystemResourceAsStream("circuit.xml"));
      } catch (Throwable t) {
        logger.warn("Unable to load circuit xml.", t);
        throw new IllegalStateException("Unable to load circuit xml.", t);
      }
    }
    circuitModel = CircuitSimulationModelBuilder.build(circuit);
  }
}
