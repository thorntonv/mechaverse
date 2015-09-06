package org.mechaverse.simulation.common.cellautomaton.simulation;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.cellautomaton.model.LogicalUnit;

import java.io.IOException;

/**
 * Builds a {@link CellularAutomatonDescriptor}.
 */
public class CellularAutomatonDescriptorBuilder {

  public static final String BOOLEAN_4INPUT_CELL_TYPE = "boolean4";

  private final CellularAutomatonDescriptor descriptor;

  public static CellularAutomatonDescriptorBuilder newBuilderFromResource(final String resourceName)
      throws IOException {
    return new CellularAutomatonDescriptorBuilder(CellularAutomatonDescriptorReader.read(
        ClassLoader.getSystemResourceAsStream(resourceName)));
  }

  public CellularAutomatonDescriptorBuilder(final CellularAutomatonDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  public CellularAutomatonDescriptorBuilder setWidth(int width) {
    descriptor.setWidth(width);
    return this;
  }

  public CellularAutomatonDescriptorBuilder setHeight(int height) {
    descriptor.setHeight(height);
    return this;
  }

  public CellularAutomatonDescriptorBuilder setIterationsPerUpdate(int iterationsPerUpdate) {
    descriptor.setIterationsPerUpdate(iterationsPerUpdate);
    return this;
  }

  public CellularAutomatonDescriptorBuilder setLogicalUnit(final LogicalUnit logicalUnit) {
    descriptor.setLogicalUnit(logicalUnit);
    return this;
  }

  public CellularAutomatonDescriptor build() {
    return descriptor;
  }
}
