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
  private final int circuitOutputSize;
  private final JavaCircuitSimulation[] circuitSimulations;
  private final CircuitAllocator allocator;

  public JavaCircuitSimulator(int numCircuits, int circuitInputSize, int circuitOutputSize,
      CircuitDataSource circuitDataSource) throws CompileException {
    this(numCircuits, circuitInputSize, circuitOutputSize, circuitDataSource.getCircuit());
  }

  public JavaCircuitSimulator(int numCircuits, int circuitInputSize, int circuitOutputSize,
      Circuit circuit) throws CompileException {
    this(numCircuits, circuitInputSize, circuitOutputSize,
        new CircuitSimulationModelBuilder().buildModel(circuit));
  }

  private JavaCircuitSimulator(int numCircuits, int circuitInputSize, int circuitOutputSize,
      CircuitSimulationModel circuitModel) throws CompileException {
    Preconditions.checkState(numCircuits > 0);
    this.circuitModel = circuitModel;
    this.circuitInputSize = circuitInputSize;
    this.circuitOutputSize = circuitOutputSize;
    this.circuitSimulations = new JavaCircuitSimulation[numCircuits];
    for (int idx = 0; idx < numCircuits; idx++) {
      circuitSimulations[idx] = compile(circuitModel, circuitInputSize, circuitOutputSize);
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
    return circuitOutputSize;
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
    circuitSimulations[circuitIndex].getOutput(circuitOutput);
  }

  @Override
  public void setCircuitOutputMap(int circuitIndex, int[] outputMap) {
    circuitSimulations[circuitIndex].setOutputMap(outputMap);
  }

  @Override
  public void update() {
    for (int idx = 0; idx < circuitSimulations.length; idx++) {
      circuitSimulations[idx].update();
    }
  }

  @Override
  public void close() throws Exception {}

  public static JavaCircuitSimulation compile(Circuit circuit,
      int circuitInputSize, int circuitOutputSize) throws CompileException {
    CircuitSimulationModelBuilder modelBuilder = new CircuitSimulationModelBuilder();
    return compile(modelBuilder.buildModel(circuit), circuitInputSize, circuitOutputSize);
  }

  public static JavaCircuitSimulation compile(CircuitSimulationModel circuitModel,
      int circuitInputSize, int circuitOutputSize) throws CompileException {
    JavaCircuitGeneratorImpl generator =
        new JavaCircuitGeneratorImpl(circuitModel, circuitInputSize, circuitOutputSize);
    StringWriter out = new StringWriter();
    generator.generate(new PrintWriter(out));
    return JavaCompilerUtil.compile(
        JavaCircuitGeneratorImpl.IMPL_PACKAGE + "." + JavaCircuitGeneratorImpl.IMPL_CLASS_NAME,
            out.toString());
  }

  @Override
  public String toString() {
    JavaCircuitGeneratorImpl generator = new JavaCircuitGeneratorImpl(circuitModel, 0, 0);
    StringWriter out = new StringWriter();
    generator.generate(new PrintWriter(out));
    return out.toString();
  }
}
