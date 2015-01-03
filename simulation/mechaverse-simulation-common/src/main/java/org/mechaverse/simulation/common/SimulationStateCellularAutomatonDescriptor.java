package org.mechaverse.simulation.common;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link CellularAutomatonDescriptorDataSource} implementation that loads a
 * {@link CellularAutomatonDescriptor} from the simulation state.
 */
public class SimulationStateCellularAutomatonDescriptor
    implements CellularAutomatonDescriptorDataSource {

  public static String DESCRIPTOR_XML_KEY = "automaton-descriptor-xml";

  private static final Logger logger =
      LoggerFactory.getLogger(SimulationStateCellularAutomatonDescriptor.class);

  @Autowired private Simulation simulation;

  private CellularAutomatonDescriptor descriptor;
  private CellularAutomatonSimulationModel model;

  @Override
  public CellularAutomatonDescriptor getDescriptor() {
    if (descriptor == null) {
      loadDescriptor();
    }
    return descriptor;
  }

  @Override
  public CellularAutomatonSimulationModel getSimulationModel() {
    if (model == null) {
      loadDescriptor();
    }
    return model;
  }

  private void loadDescriptor() {
    try {
      SimulationDataStore state = simulation.getState();
      if (state.containsKey(DESCRIPTOR_XML_KEY)) {
        descriptor = CellularAutomatonDescriptorReader.read(state.get(DESCRIPTOR_XML_KEY));
      }
    } catch (Throwable e) {
      logger.warn("Error reading cellular automaton descriptor from state");
    }

    if (descriptor == null) {
      // Attempt to load from xml on the classpath.
      try {
        descriptor = CellularAutomatonDescriptorReader.read(
            ClassLoader.getSystemResourceAsStream("boolean4.xml"));
      } catch (Throwable t) {
        logger.warn("Unable to load cellular automaton descriptor xml.", t);
        throw new IllegalStateException("Unable to load cellular automaton descriptor xml.", t);
      }
    }
    model = CellularAutomatonSimulationModelBuilder.build(descriptor);
  }
}
