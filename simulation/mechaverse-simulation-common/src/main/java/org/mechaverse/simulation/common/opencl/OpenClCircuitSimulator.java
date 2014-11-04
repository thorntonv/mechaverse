package org.mechaverse.simulation.common.opencl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.IntBuffer;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitAllocator;
import org.mechaverse.simulation.common.circuit.CircuitDataSource;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;

/**
 * Simulates circuits using an OpenCL device.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class OpenClCircuitSimulator implements CircuitSimulator {

  public static final String KERNEL_NAME = "circuit_simulation";

  private static final Logger logger = LoggerFactory.getLogger(OpenClCircuitSimulator.class);

  private final int numCircuits;
  private final int circuitInputSize;
  private final int circuitStateSize;
  private final int circuitOutputSize;
  private final long globalWorkSize;
  private final long localWorkSize;
  private final CircuitAllocator allocator;

  private final CLContext context;
  private final CLCommandQueue queue;
  private final CLKernel kernel;
  private final CLBuffer<IntBuffer> inputBuffer;
  private final CLBuffer<IntBuffer> stateBuffer;
  private final CLBuffer<IntBuffer> outputMapBuffer;
  private final CLBuffer<IntBuffer> outputBuffer;
  private boolean finished = true;
  private boolean circuitStateUpdated = false;
  private boolean outputMapUpdated = false;

  public OpenClCircuitSimulator(int numCircuits, int circuitInputSize, int circuitOutputSize,
      CircuitDataSource circuitDataSource) {
    this(numCircuits, circuitInputSize, circuitOutputSize, circuitDataSource.getCircuit());
  }

  public OpenClCircuitSimulator(
      int numCircuits, int circuitInputSize, int circuitOutputSize, Circuit circuit) {
    this(numCircuits, circuitInputSize, circuitOutputSize,
        CLPlatform.getDefault().getMaxFlopsDevice(),
            new CircuitSimulationModelBuilder().buildModel(circuit));
  }

  public OpenClCircuitSimulator(int numCircuits, int circuitInputSize,
      int circuitOutputSize, CLDevice device, Circuit circuit) {
    this(numCircuits, circuitInputSize, circuitOutputSize, device,
        new CircuitSimulationModelBuilder().buildModel(circuit));
  }

  private OpenClCircuitSimulator(int numCircuits, int circuitInputSize,
      int circuitOutputSize, CLDevice device, CircuitSimulationModel circuitModel) {
    this(numCircuits, circuitInputSize, circuitModel.getCircuitStateSize(), circuitOutputSize,
        numCircuits * circuitModel.getLogicalUnitCount(), circuitModel.getLogicalUnitCount(),
            device, getKernelSource(circuitModel));
  }

  public OpenClCircuitSimulator(int numCircuits, int circuitInputSize, int circuitStateSize,
      int circuitOutputSize, long globalWorkSize, long localWorkSize, CLDevice device,
      String kernelSource) {
    logger.debug("{}(numCircuits = {}, circuitInputSize = {}, "
        + "circuitStateSize = {}, circuitOutputSize = {})", getClass().getSimpleName(),
        numCircuits, circuitInputSize, circuitStateSize, circuitOutputSize);

    this.numCircuits = numCircuits;
    this.circuitInputSize = circuitInputSize;
    this.circuitStateSize = circuitStateSize;
    this.circuitOutputSize = circuitOutputSize;
    this.globalWorkSize = globalWorkSize;
    this.localWorkSize = localWorkSize;
    this.allocator = new CircuitAllocator(numCircuits);

    this.context = CLContext.create(device);
    CLProgram program = context.createProgram(kernelSource).build();
    this.kernel = program.createCLKernel(KERNEL_NAME);
    this.queue = device.createCommandQueue();

    // Allocate buffers.
    this.inputBuffer = context.createIntBuffer(numCircuits * circuitInputSize, Mem.READ_ONLY);
    this.stateBuffer = context.createIntBuffer(numCircuits * circuitStateSize, Mem.READ_WRITE);
    this.outputMapBuffer = context.createIntBuffer(numCircuits * circuitOutputSize, Mem.READ_WRITE);
    this.outputBuffer = context.createIntBuffer(numCircuits * circuitOutputSize, Mem.WRITE_ONLY);

    kernel.putArg(inputBuffer)
      .putArg(stateBuffer)
      .putArg(outputMapBuffer)
      .putArg(outputBuffer)
      .putArg(circuitInputSize)
      .putArg(circuitOutputSize);

    // Initialize the circuit output maps.
    int[] outputMap = new int[circuitOutputSize];
    for (int circuitIdx = 0; circuitIdx < numCircuits; circuitIdx++) {
      setCircuitOutputMap(circuitIdx, outputMap);
    }
  }

  @Override
  public CircuitAllocator getAllocator() {
    return allocator;
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
    if (circuitStateUpdated) {
      // Finish the write before starting the read.
      queue.putWriteBuffer(stateBuffer, true);
      circuitStateUpdated = false;
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
    circuitStateUpdated = true;
  }

  @Override
  public void setCircuitInput(int circuitIndex, int[] circuitInput) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    inputBuffer.getBuffer().position(circuitIndex * circuitInputSize);
    inputBuffer.getBuffer().put(circuitInput);
    inputBuffer.getBuffer().rewind();
  }

  @Override
  public void setCircuitOutputMap(int circuitIndex, int[] outputMap) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    outputMapBuffer.getBuffer().position(circuitIndex * circuitOutputSize);
    outputMapBuffer.getBuffer().put(outputMap);
    outputMapBuffer.getBuffer().rewind();
    outputMapUpdated = true;
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
    if (circuitStateUpdated) {
      queue.putWriteBuffer(stateBuffer, false);
      circuitStateUpdated = false;
    }
    if (outputMapUpdated) {
      queue.putWriteBuffer(outputMapBuffer, false);
      outputMapUpdated = false;
    }
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

    logger.debug("close()");
  }

  private static String getKernelSource(CircuitSimulationModel circuitModel) {
    OpenClCircuitGeneratorImpl generator = new OpenClCircuitGeneratorImpl(circuitModel);
    StringWriter strWriter = new StringWriter();
    PrintWriter out = new PrintWriter(strWriter);
    generator.generate(out);
    return strWriter.toString();
  }
}
