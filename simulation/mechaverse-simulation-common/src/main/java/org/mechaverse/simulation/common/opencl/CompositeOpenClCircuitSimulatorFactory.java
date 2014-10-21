package org.mechaverse.simulation.common.opencl;

import java.util.ArrayList;
import java.util.List;

import org.mechaverse.simulation.common.circuit.CircuitDataSource;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.mechaverse.simulation.common.circuit.CompositeCircuitSimulator;
import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} that creates a {@link CompositeCircuitSimulator} composed of multiple
 * {@link OpenClCircuitSimulator}s.
 */
public final class CompositeOpenClCircuitSimulatorFactory {

  private final int numCircuitGroups;
  private final int numCircuitsPerGroup;
  private final int circuitInputSize;
  private final int circuitOutputSize;
  private final CircuitDataSource circuitDataSource;

  public CompositeOpenClCircuitSimulatorFactory(int numCircuitGroups, int numCircuitsPerGroup,
      int circuitInputSize, int circuitOutputSize, CircuitDataSource circuitDataSource) {
    this.numCircuitGroups = numCircuitGroups;
    this.numCircuitsPerGroup = numCircuitsPerGroup;
    this.circuitInputSize = circuitInputSize;
    this.circuitOutputSize = circuitOutputSize;
    this.circuitDataSource = circuitDataSource;
  }

  public CompositeCircuitSimulator getObject() throws Exception {
    List<CircuitSimulator> componentSimulators = new ArrayList<>();
    for (int groupIdx = 0; groupIdx < numCircuitGroups; groupIdx++) {
      componentSimulators.add(new OpenClCircuitSimulator(numCircuitsPerGroup, circuitInputSize,
          circuitOutputSize, circuitDataSource));
    }

    return new CompositeCircuitSimulator(componentSimulators);
  }
}
