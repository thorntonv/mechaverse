package org.mechaverse.simulation.common.opencl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.IntBuffer;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLProgram;

/**
 * Simulates circuits using an OpenCL device.
 *
 * @author thorntonv@mechaverse.org
 */
public final class OpenClCircuitSimulator implements CircuitSimulator {

  public static final String KERNEL_NAME = "circuit_simulation";

  private final int numCircuits;
  private final int circuitInputSize;
  private final int circuitStateSize;
  private final int circuitOutputSize;
  private final long globalWorkSize;
  private final long localWorkSize;

  private final CLContext context;
  private final CLCommandQueue queue;
  private final CLKernel kernel;
  private final CLBuffer<IntBuffer> inputBuffer;
  private final CLBuffer<IntBuffer> stateBuffer;
  private final CLBuffer<IntBuffer> outputBuffer;
  private boolean finished = true;

  public OpenClCircuitSimulator(int numCircuits, int circuitInputSize,
      int circuitOutputSize, CLDevice device, Circuit circuit) {
    this(numCircuits, circuitInputSize, circuitOutputSize, device,
        new CircuitSimulationModelBuilder().buildModel(circuit));
  }

  private OpenClCircuitSimulator(int numCircuits, int circuitInputSize,
      int circuitOutputSize, CLDevice device, CircuitSimulationModel circuitModel) {
    this(numCircuits, circuitInputSize, numCircuits * circuitModel.getCircuitStateSize(),
        circuitOutputSize, numCircuits * circuitModel.getLogicalUnitCount(),
        circuitModel.getLogicalUnitCount(), device, getKernelSource(circuitModel));
  }

  public OpenClCircuitSimulator(int numCircuits, int circuitInputSize, int circuitStateSize,
      int circuitOutputSize, long globalWorkSize, long localWorkSize, CLDevice device,
      String kernelSource) {
    this.numCircuits = numCircuits;
    this.circuitInputSize = circuitInputSize;
    this.circuitStateSize = circuitStateSize;
    this.circuitOutputSize = circuitOutputSize;
    this.globalWorkSize = globalWorkSize;
    this.localWorkSize = localWorkSize;

    this.context = CLContext.create(device);
    CLProgram program = context.createProgram(kernelSource).build();
    this.kernel = program.createCLKernel(KERNEL_NAME);
    this.queue = device.createCommandQueue();

    // Allocate buffers.
    this.inputBuffer = context.createIntBuffer(numCircuits * circuitInputSize, Mem.READ_ONLY);
    this.stateBuffer = context.createIntBuffer(numCircuits * circuitStateSize, Mem.READ_WRITE);
    this.outputBuffer = context.createIntBuffer(numCircuits * circuitOutputSize, Mem.WRITE_ONLY);

    kernel.setArg(0, inputBuffer);
    kernel.setArg(1, stateBuffer);
    kernel.setArg(2, outputBuffer);
  }

  @Override
  public int getCircuitCount() {
    return numCircuits;
  }

  @Override
  public int getCircuitInputSize() {
    return circuitInputSize;
  }

  @Override
  public int getCircuitStateSize() {
    return circuitStateSize;
  }

  @Override
  public int getCircuitOutputSize() {
    return circuitOutputSize;
  }

  @Override
  public void getCircuitState(int circuitIndex, int[] circuitState) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    queue.putReadBuffer(stateBuffer, true);
    stateBuffer.getBuffer().position(circuitIndex * circuitStateSize);
    stateBuffer.getBuffer().get(circuitState);
    stateBuffer.getBuffer().rewind();
  }

  @Override
  public void setCircuitState(int circuitIndex, int[] circuitState) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    stateBuffer.getBuffer().position(circuitIndex * circuitStateSize);
    stateBuffer.getBuffer().put(circuitState);
    stateBuffer.getBuffer().rewind();
    queue.putWriteBuffer(stateBuffer, true);
  }

  @Override
  public void setCircuitInput(int circuitIndex, int[] circuitInput) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    inputBuffer.getBuffer().position(circuitIndex * circuitInputSize);
    inputBuffer.getBuffer().put(circuitInput);
  }

  @Override
  public void getCircuitOutput(int circuitIndex, int[] circuitOutput) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    outputBuffer.getBuffer().position(circuitIndex * circuitOutputSize);
    outputBuffer.getBuffer().get(circuitOutput);
    outputBuffer.getBuffer().rewind();
  }

  @Override
  public void update() {
    inputBuffer.getBuffer().rewind();
    queue.putWriteBuffer(inputBuffer, false)
        .put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
        .putReadBuffer(outputBuffer, false);
    finished = false;
  }

  @Override
  public void close() throws Exception {
    queue.finish();
    inputBuffer.release();
    stateBuffer.release();
    outputBuffer.release();
    queue.release();
    kernel.release();
    context.release();
  }

  private static String getKernelSource(CircuitSimulationModel circuitModel) {
    OpenClCircuitGeneratorImpl generator = new OpenClCircuitGeneratorImpl(circuitModel);
    StringWriter strWriter = new StringWriter();
    PrintWriter out = new PrintWriter(strWriter);
    generator.generate(out);
    return strWriter.toString();
  }
}
