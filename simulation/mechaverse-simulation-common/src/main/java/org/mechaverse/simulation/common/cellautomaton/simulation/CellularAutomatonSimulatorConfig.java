package org.mechaverse.simulation.common.cellautomaton.simulation;

import java.io.IOException;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;

import com.google.common.base.Preconditions;

/**
 * Used to configure {@link CellularAutomatonSimulator} instances.
 */
public class CellularAutomatonSimulatorConfig {

  public static class Builder {

    private int numAutomata = 1;
    private int automatonInputSize = 1;
    private int automatonOutputSize = 1;
    private CellularAutomatonDescriptor descriptor;

    public Builder setNumAutomata(int numAutomata) {
      this.numAutomata = numAutomata;
      return this;
    }

    public Builder setAutomatonInputSize(int automatonInputSize) {
      this.automatonInputSize = automatonInputSize;
      return this;
    }

    public Builder setAutomatonOutputSize(int automatonOutputSize) {
      this.automatonOutputSize = automatonOutputSize;
      return this;
    }

    public Builder setDescriptor(CellularAutomatonDescriptor descriptor) {
      this.descriptor = descriptor;
      return this;
    }

    public Builder setDescriptor(CellularAutomatonDescriptorDataSource descriptorDataSource) {
      return setDescriptor(descriptorDataSource.getDescriptor());
    }

    public Builder setDescriptorResource(String descriptorResourceName) throws IOException {
      return setDescriptor(CellularAutomatonDescriptorReader.read(
          ClassLoader.getSystemResourceAsStream(descriptorResourceName)));
    }

    public CellularAutomatonSimulatorConfig build() {
      Preconditions.checkNotNull(descriptor);
      return new CellularAutomatonSimulatorConfig(numAutomata, automatonInputSize,
          automatonOutputSize, descriptor);
    }
  }

  private final int numAutomata;
  private final int automatonInputSize;
  private final int automatonOutputSize;
  private final CellularAutomatonDescriptor descriptor;

  public CellularAutomatonSimulatorConfig(int numAutomata, int automatonInputSize,
      int automatonOutputSize, CellularAutomatonDescriptor descriptor) {
    this.numAutomata = numAutomata;
    this.automatonInputSize = automatonInputSize;
    this.automatonOutputSize = automatonOutputSize;
    this.descriptor = descriptor;
  }

  public int getNumAutomata() {
    return numAutomata;
  }

  public int getAutomatonInputSize() {
    return automatonInputSize;
  }

  public int getAutomatonOutputSize() {
    return automatonOutputSize;
  }

  public CellularAutomatonDescriptor getDescriptor() {
    return descriptor;
  }
}
