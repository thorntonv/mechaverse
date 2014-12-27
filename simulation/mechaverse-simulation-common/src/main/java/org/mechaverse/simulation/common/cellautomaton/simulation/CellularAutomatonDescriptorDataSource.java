package org.mechaverse.simulation.common.cellautomaton.simulation;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;

/**
 * Provides a {@link CellularAutomatonDescriptor}.
 */
public interface CellularAutomatonDescriptorDataSource {

  /**
   * Returns the cellular automaton descriptor provided by this data source.
   */
  public CellularAutomatonDescriptor getDescriptor();

  /**
   * Returns the {@link CellularAutomatonSimulationModel} for the cellular automaton provided by
   * this data source.
   */
  public CellularAutomatonSimulationModel getSimulationModel();
}
