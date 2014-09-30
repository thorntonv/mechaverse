package org.mechaverse.simulation.common.circuit.generator.java;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitAllocator;
import org.mechaverse.simulation.common.circuit.CircuitDataSource;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;
import org.mechaverse.simulation.common.util.compiler.JavaCompilerUtil;
import org.mechaverse.simulation.common.util.compiler.JavaCompilerUtil.CompileException;

import com.google.common.base.Preconditions;

/**
 * Java based circuit simulator implementation.
 *
 * @author thorntonv@mechaverse.org
 */
public final class JavaCircuitSimulator implements CircuitSimulator {

  private final CircuitSimulationModel circuitModel;
  private final int circuitInputSize;
  private final JavaCircuitSimulation[] circuitSimulations;
  private final CircuitAllocator allocator;

  public JavaCircuitSimulator(int numCircuits, int circuitInputSize, CircuitDataSource circuitDataSource)
      throws CompileException {
    this(numCircuits, circuitInputSize, circuitDataSource.getCircuit());
  }

  public JavaCircuitSimulator(int numCircuits, int circuitInputSize, Circuit circuit)
      throws CompileException {
    this(numCircuits, circuitInputSize, new CircuitSimulationModelBuilder().buildModel(circuit));
  }

  private JavaCircuitSimulator(int numCircuits, int circuitInputSize, CircuitSimulationModel circuitModel)
      throws CompileException {
    Preconditions.checkState(numCircuits > 0);
    this.circuitModel = circuitModel;
    this.circuitInputSize = circuitInputSize;
    this.circuitSimulations = new JavaCircuitSimulation[numCircuits];
    for (int idx = 0; idx < numCircuits; idx++) {
      circuitSimulations[idx] = compile(circuitModel, circuitInputSize);
    }
    allocator = new CircuitAllocator(numCircuits);
  }

  @Override
  public CircuitAllocator getAllocator() {
    return allocator;
  }

  @Override
  public int getCircuitCount() {
    return circuitSimulations.length;
  }

  @Override
  public int getCircuitInputSize() {
    return circuitInputSize;
  }

  @Override
  public int getCircuitStateSize() {
    return circuitModel.getCircuitStateSize();
  }

  @Override
  public int getCircuitOutputSize() {
    // TODO(thorntonv): Implement method.
    return 0;
  }

  @Override
  public void getCircuitState(int circuitIndex, int[] circuitState) {
    circuitSimulations[circuitIndex].getState(circuitState);
  }

  @Override
  public void setCircuitState(int circuitIndex, int[] circuitState) {
    circuitSimulations[circuitIndex].setState(circuitState);
  }

  @Override
  public void setCircuitInput(int circuitIndex, int[] circuitInput) {
    circuitSimulations[circuitIndex].setInput(circuitInput);
  }

  @Override
  public void getCircuitOutput(int circuitIndex, int[] circuitOutput) {
    // TODO(thorntonv): Implement method.
  }

  @Override
  public void update() {
    for (int idx = 0; idx < circuitSimulations.length; idx++) {
      circuitSimulations[idx].update();
    }
  }

  @Override
  public void close() throws Exception {}

  public static JavaCircuitSimulation compile(Circuit circuit, int circuitInputSize)
      throws CompileException {
    CircuitSimulationModelBuilder modelBuilder = new CircuitSimulationModelBuilder();
    return compile(modelBuilder.buildModel(circuit), circuitInputSize);
  }

  public static JavaCircuitSimulation compile(CircuitSimulationModel circuitModel,
      int circuitInputSize) throws CompileException {
    JavaCircuitGeneratorImpl generator =
        new JavaCircuitGeneratorImpl(circuitModel, circuitInputSize);
    StringWriter out = new StringWriter();
    generator.generate(new PrintWriter(out));
    return JavaCompilerUtil.compile(
        JavaCircuitGeneratorImpl.IMPL_PACKAGE + "." + JavaCircuitGeneratorImpl.IMPL_CLASS_NAME,
            out.toString());
  }

  @Override
  public String toString() {
    JavaCircuitGeneratorImpl generator = new JavaCircuitGeneratorImpl(circuitModel, 0);
    StringWriter out = new StringWriter();
    generator.generate(new PrintWriter(out));
    return out.toString();
  }
}
