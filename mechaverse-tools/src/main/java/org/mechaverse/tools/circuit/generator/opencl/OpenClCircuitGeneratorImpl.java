package org.mechaverse.tools.circuit.generator.opencl;

import java.io.PrintWriter;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.tools.circuit.generator.AbstractCircuitSimulationGenerator;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModelBuilder;

public class OpenClCircuitGeneratorImpl extends AbstractCircuitSimulationGenerator {

  public static final String TYPE = "opencl";

  public OpenClCircuitGeneratorImpl(Circuit circuit) {
    this(new CircuitSimulationModelBuilder().buildModel(circuit));
  }

  public OpenClCircuitGeneratorImpl(CircuitSimulationModel model) {
    super(model);
  }

  @Override
  public void generate(PrintWriter out) {
  }
}
