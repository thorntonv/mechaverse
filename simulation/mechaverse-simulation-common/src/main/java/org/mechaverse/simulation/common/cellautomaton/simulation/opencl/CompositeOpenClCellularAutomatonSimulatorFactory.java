package org.mechaverse.simulation.common.cellautomaton.simulation.opencl;

import java.util.ArrayList;
import java.util.List;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CompositeCellularAutomatonSimulator;
import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} that creates a {@link CompositeCellularAutomatonSimulator} composed of
 * multiple {@link OpenClCellularAutomatonSimulator}s.
 */
public final class CompositeOpenClCellularAutomatonSimulatorFactory {

  private final int numAutomatonGroups;
  private final int numAutomataPerGroup;
  private final int automatonInputSize;
  private final int automatonOutputSize;
  private final CellularAutomatonDescriptorDataSource descriptorDataSource;

  public CompositeOpenClCellularAutomatonSimulatorFactory(int numAutomatonGroups, 
      int numAutomataPerGroup, int automatonInputSize, int automatonOutputSize, 
      CellularAutomatonDescriptorDataSource descriptorDataSource) {
    this.numAutomatonGroups = numAutomatonGroups;
    this.numAutomataPerGroup = numAutomataPerGroup;
    this.automatonInputSize = automatonInputSize;
    this.automatonOutputSize = automatonOutputSize;
    this.descriptorDataSource = descriptorDataSource;
  }

  public CompositeCellularAutomatonSimulator getObject() {
    List<CellularAutomatonSimulator> componentSimulators = new ArrayList<>();
    for (int groupIdx = 0; groupIdx < numAutomatonGroups; groupIdx++) {
      componentSimulators.add(new OpenClCellularAutomatonSimulator(
          numAutomataPerGroup, automatonInputSize, automatonOutputSize, 
              descriptorDataSource.getSimulationModel()));
    }

    return new CompositeCellularAutomatonSimulator(componentSimulators);
  }
}
