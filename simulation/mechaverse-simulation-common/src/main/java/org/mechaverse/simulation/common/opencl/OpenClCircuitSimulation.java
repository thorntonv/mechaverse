package org.mechaverse.simulation.common.opencl;

import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLProgram;

/**
 * Simulates a circuit using an OpenCL device.
 *
 * @author thorntonv@mechaverse.org
 */
public final class OpenClCircuitSimulation implements AutoCloseable {

  public static final String KERNEL_NAME = "circuit_simulation";

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

  public OpenClCircuitSimulation(int numCircuits, int circuitInputSize, int circuitStateSize,
      int circuitOutputSize, long globalWorkSize, long localWorkSize, CLDevice device,
      String kernelSource) {
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

  public void getState(int circuitIndex, int[] circuitState) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    queue.putReadBuffer(stateBuffer, true);
    stateBuffer.getBuffer().position(circuitIndex * circuitStateSize);
    stateBuffer.getBuffer().get(circuitState);
    stateBuffer.getBuffer().rewind();
  }

  public void setState(int circuitIndex, int[] circuitState) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    stateBuffer.getBuffer().position(circuitIndex * circuitStateSize);
    stateBuffer.getBuffer().put(circuitState);
    stateBuffer.getBuffer().rewind();
    queue.putWriteBuffer(stateBuffer, true);
  }

  public void setInput(int circuitIndex, int[] circuitInput) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    inputBuffer.getBuffer().position(circuitIndex * circuitInputSize);
    inputBuffer.getBuffer().put(circuitInput);
  }

  public void getOutput(int circuitIndex, int[] circuitOutput) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    outputBuffer.getBuffer().position(circuitIndex * circuitOutputSize);
    outputBuffer.getBuffer().get(circuitOutput);
    outputBuffer.getBuffer().rewind();
  }

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
}
