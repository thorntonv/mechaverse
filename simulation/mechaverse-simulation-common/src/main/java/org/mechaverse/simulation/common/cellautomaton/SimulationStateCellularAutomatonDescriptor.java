package org.mechaverse.simulation.common.cellautomaton;

import com.google.common.base.Preconditions;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link CellularAutomatonDescriptorDataSource} implementation that loads a
 * {@link CellularAutomatonDescriptor} from the simulation state.
 */
public class SimulationStateCellularAutomatonDescriptor
    implements CellularAutomatonDescriptorDataSource {

  public static String DESCRIPTOR_XML_KEY = "automaton-descriptor-xml";

  private static final Logger logger =
      LoggerFactory.getLogger(SimulationStateCellularAutomatonDescriptor.class);

  private SimulationModel state;

  private CellularAutomatonDescriptor descriptor;
  private CellularAutomatonSimulationModel model;
  private String defaultDescriptorResourceName = "boolean4.xml";

  public SimulationStateCellularAutomatonDescriptor(SimulationModel state) {
    this.state = Preconditions.checkNotNull(state);
  }

  public void setDefaultDescriptorResourceName(String defaultDescriptorResourceName) {
      this.defaultDescriptorResourceName = defaultDescriptorResourceName;
  }

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
      if (state.dataContainsKey(DESCRIPTOR_XML_KEY)) {
        descriptor = CellularAutomatonDescriptorReader.read(state.getData(DESCRIPTOR_XML_KEY));
      }
    } catch (Throwable e) {
      logger.warn("Error reading cellular automaton descriptor from state");
    }

    if (descriptor == null) {
      // Attempt to load from xml on the classpath.
      try {
        descriptor = CellularAutomatonDescriptorReader.read(
            ClassLoader.getSystemResourceAsStream(defaultDescriptorResourceName));
      } catch (Throwable t) {
        logger.warn("Unable to load cellular automaton descriptor xml.", t);
        throw new IllegalStateException("Unable to load cellular automaton descriptor xml.", t);
      }
    }
    model = CellularAutomatonSimulationModelBuilder.build(descriptor);
  }
}
