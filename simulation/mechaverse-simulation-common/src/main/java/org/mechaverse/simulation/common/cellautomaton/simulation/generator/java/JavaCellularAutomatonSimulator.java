package org.mechaverse.simulation.common.cellautomaton.simulation.generator.java;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonAllocator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.mechaverse.simulation.common.util.compiler.JavaCompilerUtil;
import org.mechaverse.simulation.common.util.compiler.JavaCompilerUtil.CompileException;

import com.google.common.base.Preconditions;

/**
 * Java based cellular automaton simulator implementation.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class JavaCellularAutomatonSimulator implements CellularAutomatonSimulator {

  private final CellularAutomatonSimulationModel model;
  private final int inputSize;
  private final int outputSize;
  private final JavaCellularAutomatonSimulation[] simulations;
  private final CellularAutomatonAllocator allocator;

  public JavaCellularAutomatonSimulator(int numAutomata, int inputSize, int outputSize,
      CellularAutomatonDescriptorDataSource dataSource) throws CompileException {
    this(numAutomata, inputSize, outputSize, dataSource.getDescriptor());
  }

  public JavaCellularAutomatonSimulator(int numAutomata, int inputSize, int outputSize,
      CellularAutomatonDescriptor descriptor) throws CompileException {
    this(numAutomata, inputSize, outputSize,
        new CellularAutomatonSimulationModelBuilder().buildModel(descriptor));
  }

  private JavaCellularAutomatonSimulator(int numAutomata, int inputSize, int outputSize,
      CellularAutomatonSimulationModel model) throws CompileException {
    Preconditions.checkState(numAutomata > 0);
    this.model = model;
    this.inputSize = inputSize;
    this.outputSize = outputSize;
    this.simulations = new JavaCellularAutomatonSimulation[numAutomata];
    for (int idx = 0; idx < numAutomata; idx++) {
      simulations[idx] = compile(model, inputSize, outputSize);
    }
    allocator = new CellularAutomatonAllocator(numAutomata);
  }

  @Override
  public CellularAutomatonAllocator getAllocator() {
    return allocator;
  }

  @Override
  public int size() {
    return simulations.length;
  }

  @Override
  public int getAutomatonInputSize() {
    return inputSize;
  }

  @Override
  public int getAutomatonStateSize() {
    return model.getStateSize();
  }

  @Override
  public int getAutomatonOutputSize() {
    return outputSize;
  }

  @Override
  public void getAutomatonState(int index, int[] state) {
    simulations[index].getState(state);
  }

  @Override
  public void setAutomatonState(int index, int[] state) {
    simulations[index].setState(state);
  }

  @Override
  public void setAutomatonInput(int index, int[] input) {
    simulations[index].setInput(input);
  }

  @Override
  public void getAutomatonOutput(int index, int[] output) {
    simulations[index].getOutput(output);
  }

  @Override
  public void setAutomatonOutputMap(int index, int[] outputMap) {
    simulations[index].setOutputMap(outputMap);
  }

  @Override
  public void update() {
    for (int idx = 0; idx < simulations.length; idx++) {
      simulations[idx].update();
    }
  }

  @Override
  public void close() throws Exception {}

  public static JavaCellularAutomatonSimulation compile(CellularAutomatonDescriptor descriptor,
      int inputSize, int outputSize) throws CompileException {
    CellularAutomatonSimulationModelBuilder modelBuilder =
        new CellularAutomatonSimulationModelBuilder();
    return compile(modelBuilder.buildModel(descriptor), inputSize, outputSize);
  }

  public static JavaCellularAutomatonSimulation compile(CellularAutomatonSimulationModel model,
      int inputSize, int outputSize) throws CompileException {
    JavaCellularAutomatonGeneratorImpl generator =
        new JavaCellularAutomatonGeneratorImpl(model, inputSize, outputSize);
    StringWriter out = new StringWriter();
    generator.generate(new PrintWriter(out));
    return JavaCompilerUtil.compile(JavaCellularAutomatonGeneratorImpl.IMPL_PACKAGE 
        + "." + JavaCellularAutomatonGeneratorImpl.IMPL_CLASS_NAME, out.toString());
  }

  @Override
  public String toString() {
    JavaCellularAutomatonGeneratorImpl generator =
        new JavaCellularAutomatonGeneratorImpl(model, 0, 0);
    StringWriter out = new StringWriter();
    generator.generate(new PrintWriter(out));
    return out.toString();
  }
}
