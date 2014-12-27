package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.ConnectionInfo;

/**
 * Builder for {@link ConnectionInfo}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface ConnectionInfoBuilder {

  public ConnectionInfo build();
}
