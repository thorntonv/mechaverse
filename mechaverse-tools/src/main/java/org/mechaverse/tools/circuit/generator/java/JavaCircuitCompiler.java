package org.mechaverse.tools.circuit.generator.java;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModelBuilder;
import org.mechaverse.tools.util.compiler.JavaCompilerUtil;
import org.mechaverse.tools.util.compiler.JavaCompilerUtil.CompileException;

/**
 * Generates and compiles an executable {@link CircuitSimulation} for a {@link Circuit}.
 *
 * @author thorntonv@mechaverse.org
 */
public class JavaCircuitCompiler {

  private static final String IMPL_CLASS_NAME =
      "org.mechaverse.tools.circuit.generator.java.CircuitSimulationImpl";

  private final CircuitSimulationModelBuilder modelBuilder = new CircuitSimulationModelBuilder();

  public CircuitSimulation compile(Circuit circuit) throws CompileException {
    CircuitSimulationModel model = modelBuilder.buildModel(circuit);
    JavaCircuitGeneratorImpl generator = new JavaCircuitGeneratorImpl(model);
    StringWriter out = new StringWriter();
    generator.generate(new PrintWriter(out));
    return JavaCompilerUtil.compile(IMPL_CLASS_NAME, out.toString());
  }
}
