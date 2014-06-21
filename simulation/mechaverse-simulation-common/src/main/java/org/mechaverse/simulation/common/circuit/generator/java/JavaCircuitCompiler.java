package org.mechaverse.simulation.common.circuit.generator.java;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;
import org.mechaverse.simulation.common.util.compiler.JavaCompilerUtil;
import org.mechaverse.simulation.common.util.compiler.JavaCompilerUtil.CompileException;

/**
 * Generates and compiles an executable {@link CircuitSimulation} for a {@link Circuit}.
 *
 * @author thorntonv@mechaverse.org
 */
public class JavaCircuitCompiler {

  private final CircuitSimulationModelBuilder modelBuilder = new CircuitSimulationModelBuilder();

  private final Logger logger = Logger.getLogger(JavaCircuitCompiler.class);

  public CircuitSimulation compile(Circuit circuit) throws CompileException {
    CircuitSimulationModel model = modelBuilder.buildModel(circuit);
    JavaCircuitGeneratorImpl generator = new JavaCircuitGeneratorImpl(model);
    StringWriter out = new StringWriter();
    generator.generate(new PrintWriter(out));
    logger.debug("Compiling source: " + out.toString());
    return JavaCompilerUtil.compile(
      JavaCircuitGeneratorImpl.IMPL_PACKAGE + "." + JavaCircuitGeneratorImpl.IMPL_CLASS_NAME,
          out.toString());
  }
}
